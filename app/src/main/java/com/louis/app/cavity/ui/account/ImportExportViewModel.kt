package com.louis.app.cavity.ui.account

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.repository.AccountRepository
import com.louis.app.cavity.domain.backup.BackupBuilder
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.HistoryRepository
import com.louis.app.cavity.network.response.ApiResponse
import com.louis.app.cavity.worker.AutoUploadWorker
import com.louis.app.cavity.worker.AutoUploadWorker.Companion.WORK_DATA_HEALTHCHECK_ONLY
import com.louis.app.cavity.worker.DownloadWorker
import com.louis.app.cavity.worker.PruneWorker
import com.louis.app.cavity.worker.UploadWorker
import com.louis.app.cavity.ui.BaseViewModel
import com.louis.app.cavity.util.toBoolean
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

sealed interface ImportExportEvent {
    data class UserFeedback(@StringRes val resId: Int) : ImportExportEvent
    data class UserFeedbackString(val message: String) : ImportExportEvent
    data object NavigateToLogin : ImportExportEvent
}

data class ImportExportUiState(
    val isLoading: Boolean = false,
    val health: Int? = null,
    val distantBottleCount: Int? = null,
    val localBottleCount: Int? = null
)

class ImportExportViewModel(app: Application) : BaseViewModel<ImportExportUiState, ImportExportEvent>(app, ImportExportUiState()) {

    companion object {
        private const val MIN_BACKOFF_SECONDS = 10L
        private const val AUTO_BACKUP_PERIODICITY_IN_DAYS = 15L
        private const val AUTO_BACKUP_INITIAL_DELAY_IN_HOURS = 1L
    }

    private val historyRepository = HistoryRepository.getInstance(app)
    private val bottleRepository = BottleRepository.getInstance(app)
    private val accountRepository = AccountRepository.getInstance(app)
    private val workManager = WorkManager.getInstance(app)
    private val backupBuilder = BackupBuilder(app)

    private val _workRequestId = MutableStateFlow<UUID?>(null)
    val workProgress: Flow<WorkInfo?> = _workRequestId.flatMapLatest { id ->
        if (id != null) workManager.getWorkInfoByIdFlow(id)
        else kotlinx.coroutines.flow.flowOf(null)
    }

    private val _autoBackupWorkRequestId = MutableStateFlow<UUID?>(null)
    val autoBackupWorkProgress: Flow<WorkInfo?> = _autoBackupWorkRequestId.flatMapLatest { id ->
        if (id != null) workManager.getWorkInfoByIdFlow(id)
        else kotlinx.coroutines.flow.flowOf(null)
    }

    private val _healthCheckWorkRequestId = MutableStateFlow<UUID?>(null)
    val healthCheckWorkProgress: Flow<WorkInfo?> = _healthCheckWorkRequestId.flatMapLatest { id ->
        if (id != null) workManager.getWorkInfoByIdFlow(id)
        else kotlinx.coroutines.flow.flowOf(null)
    }

    var preventHealthCheckSpam = false
        get() = field.also { field = true }

    fun fetchHealth(isImport: Boolean) {
        val isExport = !isImport

        viewState = viewState.copy(isLoading = true)

        viewModelScope.launch(IO) {
            try {
                val localEntries = historyRepository.getAllEntriesNotPagedNotLive()
                accountRepository.getHistoryEntries().let { response ->
                    when (response) {
                        is ApiResponse.Success -> {
                            val distantEntries = response.value
                            val target = if (isExport) distantEntries else localEntries
                            val source = if (isExport) localEntries else distantEntries
                            val health = backupBuilder.checkHealth(source, target)
                            val stringRes = backupBuilder.getTextForHealthResult(health, isExport)
                            viewState = viewState.copy(health = stringRes)
                        }

                        is ApiResponse.Failure -> emitEvent(ImportExportEvent.UserFeedbackString(response.message))
                        is ApiResponse.UnknownError -> emitEvent(ImportExportEvent.UserFeedback(R.string.base_error))
                        is ApiResponse.UnauthorizedError -> emitEvent(ImportExportEvent.NavigateToLogin)
                        is ApiResponse.UnregisteredError -> Unit
                    }
                }
            } finally {
                viewState = viewState.copy(isLoading = false)
            }
        }
    }

    fun fetchDistantBottleCount() {
        viewState = viewState.copy(isLoading = true)

        viewModelScope.launch(IO) {
            try {
                when (val response = accountRepository.getBottles()) {
                    is ApiResponse.Success -> {
                        val count = response.value.count { !it.consumed.toBoolean() }
                        viewState = viewState.copy(distantBottleCount = count)
                    }

                    is ApiResponse.Failure -> emitEvent(ImportExportEvent.UserFeedbackString(response.message))
                    is ApiResponse.UnknownError -> emitEvent(ImportExportEvent.UserFeedback(R.string.base_error))
                    is ApiResponse.UnauthorizedError -> emitEvent(ImportExportEvent.NavigateToLogin)
                    is ApiResponse.UnregisteredError -> Unit
                }
            } finally {
                viewState = viewState.copy(isLoading = false)
            }
        }
    }

    fun fetchLocalBottleCount() {
        viewModelScope.launch(IO) {
            val count = bottleRepository.getAllBottlesNotLive().count { !it.consumed.toBoolean() }
            viewState = viewState.copy(localBottleCount = count)
        }
    }

    fun export() {
        if (viewState.isLoading) return

        workManager.cancelAllWorkByTag(UploadWorker.WORK_TAG)

        OneTimeWorkRequestBuilder<UploadWorker>()
            .addTag(UploadWorker.WORK_TAG)
            .setBackoffCriteria(BackoffPolicy.LINEAR, MIN_BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build().also {
                _workRequestId.value = it.id
                workManager.enqueue(it)
            }
    }

    fun import() {
        if (viewState.isLoading) return

        workManager.cancelAllWorkByTag(DownloadWorker.WORK_TAG)

        OneTimeWorkRequestBuilder<DownloadWorker>()
            .addTag(DownloadWorker.WORK_TAG)
            .setBackoffCriteria(BackoffPolicy.LINEAR, MIN_BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build().also {
                _workRequestId.value = it.id
                workManager.enqueue(it)
            }
    }

    fun pruneWorks() = workManager.pruneWork()

    fun autoBackupHealthCheck() {
        workManager.cancelAllWorkByTag(AutoUploadWorker.WORK_TAG_HEALTHCHECK)

        OneTimeWorkRequestBuilder<AutoUploadWorker>()
            .addTag(AutoUploadWorker.WORK_TAG_HEALTHCHECK)
            .setInputData(Data.Builder().putBoolean(WORK_DATA_HEALTHCHECK_ONLY, true).build())
            .build().also {
                _healthCheckWorkRequestId.value = it.id
                workManager.enqueue(it)
            }
    }

    fun enableAutoBackups() {
        cancelCurrentAutoBackup()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        PeriodicWorkRequestBuilder<AutoUploadWorker>(AUTO_BACKUP_PERIODICITY_IN_DAYS, TimeUnit.DAYS)
            .addTag(AutoUploadWorker.WORK_TAG)
            .setInitialDelay(AUTO_BACKUP_INITIAL_DELAY_IN_HOURS, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build().also {
                _autoBackupWorkRequestId.value = it.id
                workManager.enqueue(it)
            }
    }

    fun cancelCurrentAutoBackup() {
        workManager.cancelAllWorkByTag(AutoUploadWorker.WORK_TAG)
    }

    fun cleanAccountDatabase() {
        workManager.cancelAllWorkByTag(PruneWorker.WORK_TAG)

        OneTimeWorkRequestBuilder<PruneWorker>()
            .addTag(PruneWorker.WORK_TAG)
            .setBackoffCriteria(BackoffPolicy.LINEAR, MIN_BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build().also {
                _workRequestId.value = it.id
                workManager.enqueue(it)
            }
    }
}

