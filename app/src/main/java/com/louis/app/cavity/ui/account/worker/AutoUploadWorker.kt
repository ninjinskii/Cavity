package com.louis.app.cavity.ui.account.worker

import android.app.Application
import android.content.Context
import androidx.annotation.StringRes
import androidx.work.CoroutineWorker
import androidx.work.Data
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
            val health = checkHealth()
            if (health == HealthResult.OK) {
                uploadDatabase()
                sendNotification(R.string.auto_backup_done_title, R.string.auto_backup_done)
                prefsRepository.setLastAutoBackupResult(health.stringRes)

                val data = Data.Builder().putInt(WORK_DATA_HEALTH_STATE, health.stringRes).build()
                Result.success(data)
            } else {
                val message = when(health) {
                    HealthResult.PREVENT_OVERWRITE -> {
                        R.string.auto_backup_overwrite_data
                    }
                    HealthResult.NOT_MATCHING -> {
                        R.string.auto_backup_not_matching
                    }
                    else /* HealthResult.FAILED */ -> {
                        R.string.auto_backup_unavailable
                    }
                }

                sendNotification(
                    R.string.auto_backup_failed_title,
                    message
                )
                prefsRepository.setLastAutoBackupResult(health.stringRes)

                val data = Data.Builder().putInt(WORK_DATA_HEALTH_STATE, health.stringRes).build()
                Result.failure(data)
            }
        } catch (e: UncompleteExportException) {
            if (runAttemptCount < 1) {
                Result.retry()
            } else {
                sendNotification(R.string.auto_backup_failed_title, R.string.base_error)
                prefsRepository.setLastAutoBackupResult(HealthResult.FAILED.stringRes)
                Sentry.captureException(e)
                val data =
                    Data.Builder().putInt(WORK_DATA_HEALTH_STATE, HealthResult.FAILED.stringRes).build()

                Result.failure(data)
            }
        } catch (e: Exception) {
            sendNotification(R.string.auto_backup_failed_title, R.string.base_error)
            prefsRepository.setLastAutoBackupResult(HealthResult.FAILED.stringRes)
            Sentry.captureException(e)
            val data =
                Data.Builder().putInt(WORK_DATA_HEALTH_STATE, HealthResult.FAILED.stringRes).build()

            Result.failure(data)
        }
    }

    private suspend fun checkHealth() = withContext(IO) {
        accountRepository.getHistoryEntries().let { response ->
            when (response) {
                is ApiResponse.Success -> checkHealth(response.value)
                is ApiResponse.Failure -> HealthResult.FAILED
                is ApiResponse.UnknownError -> HealthResult.FAILED
                is ApiResponse.UnauthorizedError -> {
                    prefsRepository.setApiToken("")
                    sendNotification(
                        R.string.auto_backup_failed_title,
                        R.string.auto_backup_unauthorized
                    )
                    HealthResult.FAILED
                }

                is ApiResponse.UnregisteredError -> HealthResult.FAILED
            }
        }
    }

    private suspend fun checkHealth(distantHistoryEntries: List<HistoryEntry>): HealthResult {
        val localHistoryEntries = repository.getAllEntriesNotPagedNotLive()
        val distantMinMax = distantHistoryEntries.minMaxByOrNull { it.date }
        val localMinMax = localHistoryEntries.minMaxByOrNull { it.date }
        val distantOldest = distantMinMax?.first?.date ?: 0
        val distantNewest = distantMinMax?.second?.date ?: 0
        val localOldest = localMinMax?.first?.date ?: 0
        val localNewest = localMinMax?.second?.date ?: 0

        return when {
            localOldest != distantOldest -> HealthResult.NOT_MATCHING
            localNewest < distantNewest -> HealthResult.PREVENT_OVERWRITE
            else -> HealthResult.OK
        }
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

    private fun <T> Iterable<T>.minMaxByOrNull(selector: (T) -> Long): Pair<T, T>? {
        val iterator = iterator()

        if (!iterator.hasNext()) {
            return null
        }

        var max = iterator.next()
        var min = max

        if (!iterator.hasNext()) {
            return min to max
        }

        var maxValue = selector(max)
        var minValue = selector(min)

        do {
            val element = iterator.next()
            val value = selector(element)

            if (maxValue < value) {
                max = element
                maxValue = value
            }

            if (minValue > value) {
                min = element
                minValue = value
            }
        } while (iterator.hasNext())

        return min to max
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
        const val WORK_DATA_HEALTH_STATE = "com.louis.app.cavity.WORK_DATA_HEALTH_STATE"
    }

    enum class HealthResult(@StringRes val stringRes: Int) {
        OK(R.string.backup_status_active),
        NOT_MATCHING(R.string.backup_status_suspicious),
        PREVENT_OVERWRITE(R.string.backup_status_pause),
        FAILED(R.string.backup_status_error)
    }
}
