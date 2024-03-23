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
import com.louis.app.cavity.domain.backup.AutoBackup
import com.louis.app.cavity.domain.backup.BackupFinishedListener
import com.louis.app.cavity.ui.notifications.NotificationBuilder
import com.louis.app.cavity.util.L
import io.sentry.Sentry
import kotlinx.coroutines.delay

class AutoUploadWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    private val repository = WineRepository.getInstance(context as Application)
    private val accountRepository = AccountRepository.getInstance(context as Application)
    private val prefsRepository = PrefsRepository.getInstance(context as Application)

    override suspend fun doWork(): Result {
        val listener = object : BackupFinishedListener<Data> {
            override fun onSuccess(): Data {
                sendNotification(R.string.auto_backup_done_title, R.string.auto_backup_done)

                return Data.Builder()
                    .putInt(WORK_DATA_HEALTH_STATE_KEY, HEALTH_STATE_SUCCESS)
                    .build()
            }

            override fun onFailure(canRetry: Boolean, exception: Exception?): Data {
                exception?.let { Sentry.captureException(it) }
                sendNotification(R.string.auto_backup_failed_title, R.string.base_error)

                val data = Data.Builder()
                    .putInt(WORK_DATA_HEALTH_STATE_KEY, HEALTH_STATE_FAILED)
                    .build()

                return data
//                return if (canRetry && runAttemptCount < 1) Result.retry() else Result.failure(data)
            }

            override fun onUnauthorized(): Data {
                prefsRepository.setApiToken("")
                sendNotification(
                    R.string.auto_backup_failed_title,
                    R.string.auto_backup_unauthorized
                )

                return Data.Builder()
                    .putInt(WORK_DATA_HEALTH_STATE_KEY, HEALTH_STATE_UNAUTHORIZED)
                    .build()
            }

            override fun onPreventOverwriting(): Data {
                sendNotification(
                    R.string.auto_backup_failed_title,
                    R.string.auto_backup_overwrite_data
                )

                return Data.Builder()
                    .putInt(WORK_DATA_HEALTH_STATE_KEY, HEALTH_STATE_PREVENT_OVERWRITE)
                    .build()
            }

            override fun onPreventAccountSwitch(): Data {
                sendNotification(
                    R.string.auto_backup_failed_title,
                    R.string.auto_backup_not_matching
                )

                return Data.Builder()
                    .putInt(WORK_DATA_HEALTH_STATE_KEY, HEALTH_STATE_PREVENT_ACCOUNT_SWITCH)
                    .build()
            }
        }

        val autoBackup = AutoBackup(repository, accountRepository, context, listener)

        return try {
            val result = autoBackup.tryBackup()
            L.v("setProgerss")
            L.v(result.toString())
            setProgress(result)
            delay(2000)
            Result.success()
        } catch (e: Exception) {
            Sentry.captureException(e)
            val data = Data.Builder()
                .putInt(WORK_DATA_HEALTH_STATE_KEY, HEALTH_STATE_FAILED)
                .build()

            setProgress(data)
            Result.failure()
        }
    }

    private fun sendNotification(@StringRes title: Int, @StringRes content: Int) {
        val notification = NotificationBuilder.buildAutoBackupNotification(context, title, content)
        NotificationBuilder.notify(context, notification)
    }

    companion object {
        const val WORK_TAG = "com.louis.app.cavity.auto-upload-db"
        const val WORK_DATA_HEALTH_STATE_KEY = "com.louis.app.cavity.WORK_DATA_HEALTH_STATE_KEY"
        const val HEALTH_STATE_SUCCESS = 0
        const val HEALTH_STATE_FAILED = 1
        const val HEALTH_STATE_PREVENT_OVERWRITE = 2
        const val HEALTH_STATE_PREVENT_ACCOUNT_SWITCH = 3
        const val HEALTH_STATE_UNAUTHORIZED = 4
        const val HEALTH_STATE_USER_DISABLED = 5
    }

}
