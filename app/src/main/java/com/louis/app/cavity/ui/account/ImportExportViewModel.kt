package com.louis.app.cavity.ui.account

import android.app.Application
import androidx.lifecycle.*
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.louis.app.cavity.db.AccountRepository
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.network.response.ApiResponse
import com.louis.app.cavity.ui.account.worker.DownloadWorker
import com.louis.app.cavity.ui.account.worker.UploadWorker
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class ImportExportViewModel(app: Application) : AndroidViewModel(app) {
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

    fun checkHealth(isImport: Boolean) {
        val isExport = !isImport

        viewModelScope.launch(IO) {
            accountRepository.getHistoryEntries().let { response ->
                if (response is ApiResponse.Success) {
                    val distantHistoryEntries = response.value
                    val localHistoryEntries = repository.getAllEntriesNotPagedNotLive()
                    val distantNewer = distantHistoryEntries.maxByOrNull { it.date }?.date ?: 0
                    val localNewer = localHistoryEntries.maxByOrNull { it.date }?.date ?: 0
                    val healthy =
                        if (isExport) localNewer >= distantNewer else distantNewer >= localNewer

                    _healthy.postValue(healthy)
                }
            }
        }
    }

    fun export() {
        workManager.cancelAllWorkByTag(UploadWorker.WORK_TAG)

        OneTimeWorkRequestBuilder<UploadWorker>()
            .addTag(UploadWorker.WORK_TAG)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS, // 10 sec
                TimeUnit.MILLISECONDS
            )
            .build().also {
                workRequestId.value = it.id
                workManager.enqueue(it)
            }
    }

    fun import() {
        workManager.cancelAllWorkByTag(DownloadWorker.WORK_TAG)

        OneTimeWorkRequestBuilder<DownloadWorker>()
            .addTag(DownloadWorker.WORK_TAG)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS, // 10 sec
                TimeUnit.MILLISECONDS
            )
            .build().also {
                //workRequestId.value = it.id
                workManager.enqueue(it)
            }
    }
}
