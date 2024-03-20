package com.louis.app.cavity.ui.account.worker

import android.app.Application
import android.content.Context
import androidx.annotation.StringRes
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.louis.app.cavity.R
import com.louis.app.cavity.db.AccountRepository
import com.louis.app.cavity.db.PrefsRepository
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.FileAssoc
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.network.response.ApiResponse
import com.louis.app.cavity.ui.account.Environment
import com.louis.app.cavity.ui.notifications.NotificationBuilder
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AutoUploadWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    private val repository = WineRepository.getInstance(context as Application)
    private val accountRepository = AccountRepository.getInstance(context as Application)
    private val prefsRepository = PrefsRepository.getInstance(context as Application)

    override suspend fun doWork(): Result {
        return try {
            if (!checkHealth()) {
                uploadDatabase()
                sendNotification(R.string.auto_backup_done_title, R.string.auto_backup_done)
                Result.success()
            } else {
                sendNotification(R.string.auto_backup_failed_title, R.string.auto_backup_overwrite_data)
                Result.failure()
            }
        } catch (e: UncompleteExportException) {
            if (runAttemptCount < 1) {
                Result.retry()
            } else {
                sendNotification(R.string.auto_backup_failed_title, R.string.base_error)
                Sentry.captureException(e)
                Result.failure()
            }
        } catch (e: Exception) {
            sendNotification(R.string.auto_backup_failed_title, R.string.base_error)
            Sentry.captureException(e)
            Result.failure()
        }
    }

    private suspend fun checkHealth() = withContext(IO) {
        accountRepository.getHistoryEntries().let { response ->
            when (response) {
                is ApiResponse.Success -> checkHealth(isExport = true, response.value)
                is ApiResponse.Failure -> false
                is ApiResponse.UnknownError -> false
                is ApiResponse.UnauthorizedError -> {
                    prefsRepository.setApiToken("")
                    sendNotification(
                        R.string.auto_backup_failed_title,
                        R.string.auto_backup_unauthorized
                    )
                    false
                }

                is ApiResponse.UnregisteredError -> false
            }
        }
    }

    private suspend fun checkHealth(
        isExport: Boolean,
        distantHistoryEntries: List<HistoryEntry>
    ): Boolean {
        val localHistoryEntries = repository.getAllEntriesNotPagedNotLive()
        val distantNewer =
            distantHistoryEntries.maxByOrNull { it.date }?.date ?: 0
        val localNewer = localHistoryEntries.maxByOrNull { it.date }?.date ?: 0

        return if (isExport) localNewer >= distantNewer
        else distantNewer >= localNewer
    }

    private suspend fun uploadDatabase() = withContext(IO) {
        with(accountRepository) {
            launch {
                val wines = repository.getAllWinesNotLive()
                val bottles = repository.getAllBottlesNotLive()
                val friends = repository.getAllFriendsNotLive()

                // Get wines & bottles first, copy them to external dir
                copyToExternalDir(wines + bottles + friends)

                listOf(
                    postCounties(repository.getAllCountiesNotLive()),
                    postWines(wines),
                    postBottles(bottles),
                    postFriends(friends),
                    postGrapes(repository.getAllGrapesNotLive()),
                    postReviews(repository.getAllReviewsNotLive()),
                    postHistoryEntries(repository.getAllEntriesNotPagedNotLive()),
                    postTastings(repository.getAllTastingsNotLive()),
                    postTastingActions(repository.getAllTastingActionsNotLive()),
                    postFReviews(repository.getAllFReviewsNotLive()),
                    postQGrapes(repository.getAllQGrapesNotLive()),
                    postTastingFriendsXRefs(repository.getAllTastingXFriendsNotLive()),
                    postHistoryFriendsXRefs(repository.getAllHistoryXFriendsNotLive())
                ).forEach {
                    if (it !is ApiResponse.Success) {
                        throw UncompleteExportException()
                    }
                }

                postAccountLastUser(Environment.getDeviceName())
            }
        }
    }

    private fun copyToExternalDir(fileAssocs: List<FileAssoc>) {
        fileAssocs
            .forEach {
                FileProcessor(context, it).run {
                    copyToExternalDir()
                }
            }
    }

    private fun sendNotification(@StringRes title: Int, @StringRes content: Int) {
        val notification = NotificationBuilder.buildAutoBackupNotification(context, title, content)
        NotificationBuilder.notify(context, notification)
    }

    class UncompleteExportException : Exception()

    companion object {
        const val WORK_TAG = "com.louis.app.cavity.auto-upload-db"
    }
}
