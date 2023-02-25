package com.louis.app.cavity.ui.account

import android.app.Application
import androidx.lifecycle.*
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.louis.app.cavity.R
import com.louis.app.cavity.db.AccountRepository
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.network.response.ApiResponse
import com.louis.app.cavity.ui.account.worker.DownloadWorker
import com.louis.app.cavity.ui.account.worker.UploadWorker
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toBoolean
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class ImportExportViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        private const val MIN_BACKOFF_SECONDS = 10L
    }

    private val repository = WineRepository.getInstance(app)
    private val accountRepository = AccountRepository.getInstance(app)
    private val workManager = WorkManager.getInstance(app)

    private val workRequestId = MutableLiveData<UUID>()
    val workProgress = workRequestId.switchMap {
        workManager.getWorkInfoByIdLiveData(it)
    }

    // Determines whether or not the data we want to export are older than the backup data
    private val _healthy = MutableLiveData(true)
    val healthy: LiveData<Boolean>
        get() = _healthy

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _distantBottleCount = MutableLiveData<Int>()
    val distantBottleCount: LiveData<Int>
        get() = _distantBottleCount

    private val _localBottleCount = MutableLiveData<Int>()
    val localBottleCount: LiveData<Int>
        get() = _localBottleCount

    private val _navigateToLogin = MutableLiveData<Event<Unit>>()
    val navigateToLogin: LiveData<Event<Unit>>
        get() = _navigateToLogin

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private val _userFeedbackString = MutableLiveData<Event<String>>()
    val userFeedbackString: LiveData<Event<String>>
        get() = _userFeedbackString

    fun checkHealth(isImport: Boolean) {
        val isExport = !isImport

        _isLoading.value = true

        viewModelScope.launch(IO) {
            try {
                accountRepository.getHistoryEntries().let { response ->
                    when (response) {
                        is ApiResponse.Success -> {
                            val distantHistoryEntries = response.value
                            val localHistoryEntries = repository.getAllEntriesNotPagedNotLive()
                            val distantNewer =
                                distantHistoryEntries.maxByOrNull { it.date }?.date ?: 0
                            val localNewer = localHistoryEntries.maxByOrNull { it.date }?.date ?: 0
                            val healthy =
                                if (isExport) localNewer >= distantNewer
                                else distantNewer >= localNewer

                            _healthy.postValue(healthy)
                        }

                        is ApiResponse.Failure -> _userFeedbackString.postOnce(response.message)
                        is ApiResponse.UnknownError -> _userFeedback.postOnce(R.string.base_error)
                        is ApiResponse.UnauthorizedError -> _navigateToLogin.postOnce(Unit)
                        is ApiResponse.UnregisteredError -> Unit
                    }
                }
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchDistantBottleCount() {
        _isLoading.postValue(true)

        viewModelScope.launch(IO) {
            try {
                when (val response = accountRepository.getBottles()) {
                    is ApiResponse.Success -> {
                        val count = response.value.count { !it.consumed.toBoolean() }
                        _distantBottleCount.postValue(count)
                    }
                    is ApiResponse.Failure -> _userFeedbackString.postOnce(response.message)
                    is ApiResponse.UnknownError -> _userFeedback.postOnce(R.string.base_error)
                    is ApiResponse.UnauthorizedError -> _navigateToLogin.postOnce(Unit)
                    is ApiResponse.UnregisteredError -> Unit
                }
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchLocalBottleCount() {
        viewModelScope.launch(IO) {
            val count = repository.getAllBottlesNotLive().count { !it.consumed.toBoolean() }
            _localBottleCount.postValue(count)
        }
    }

    fun export() {
        if (_isLoading.value == true) {
            return
        }

        workManager.cancelAllWorkByTag(UploadWorker.WORK_TAG)

        OneTimeWorkRequestBuilder<UploadWorker>()
            .addTag(UploadWorker.WORK_TAG)
            .setBackoffCriteria(BackoffPolicy.LINEAR, MIN_BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build().also {
                workRequestId.value = it.id
                workManager.enqueue(it)
            }
    }

    fun import() {
        if (_isLoading.value == true) {
            return
        }

        workManager.cancelAllWorkByTag(DownloadWorker.WORK_TAG)

        OneTimeWorkRequestBuilder<DownloadWorker>()
            .addTag(DownloadWorker.WORK_TAG)
            .setBackoffCriteria(BackoffPolicy.LINEAR, MIN_BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build().also {
                workRequestId.value = it.id
                workManager.enqueue(it)
            }
    }

    fun pruneWorks() = workManager.pruneWork()
}
