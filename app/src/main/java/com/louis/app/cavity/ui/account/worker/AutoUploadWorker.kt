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
import io.sentry.Sentry

class AutoUploadWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    private val repository = WineRepository.getInstance(context as Application)
    private val accountRepository = AccountRepository.getInstance(context as Application)
    private val prefsRepository = PrefsRepository.getInstance(context as Application)

    override suspend fun doWork(): Result {
        val listener = object : BackupFinishedListener<Result> {
            override fun onSuccess(): Result {
                sendNotification(R.string.auto_backup_done_title, R.string.auto_backup_done)
                val data = Data.Builder().put().build() // to think
                return Result.success(data)
            }

            override fun onFailure(canRetry: Boolean, exception: Exception?): Result {
                exception?.let { Sentry.captureException(it) }
                sendNotification(R.string.auto_backup_failed_title, R.string.base_error)
                val data = Data.Builder().put().build()
                return if (canRetry && runAttemptCount < 1) Result.retry() else Result.failure(data)
            }

            override fun onUnauthorized(): Result {
                prefsRepository.setApiToken("")
                sendNotification(
                    R.string.auto_backup_failed_title,
                    R.string.auto_backup_unauthorized
                )

                val data = Data.Builder().put().build()
                return Result.failure(data)
            }

            override fun onPreventOverwriting(): Result {
                sendNotification(
                    R.string.auto_backup_failed_title,
                    R.string.auto_backup_overwrite_data
                )

                val data = Data.Builder().put().build()
                return Result.failure(data)
            }

            override fun onPreventAccountSwitch(): Result {
                sendNotification(
                    R.string.auto_backup_failed_title,
                    R.string.auto_backup_not_matching
                )

                val data = Data.Builder().put().build()
                return Result.failure(data)
            }
        }

        val autoBackup = AutoBackup(repository, accountRepository, context, listener)

        return try {
            autoBackup.tryBackup()
        } catch (e: Exception) {
            Sentry.captureException(e)
            val data = Data.Builder().put().build()
            Result.failure(data)
        }
    }

    private fun sendNotification(@StringRes title: Int, @StringRes content: Int) {
        val notification = NotificationBuilder.buildAutoBackupNotification(context, title, content)
        NotificationBuilder.notify(context, notification)
    }

    companion object {
        const val WORK_TAG = "com.louis.app.cavity.auto-upload-db"
        const val WORK_DATA_HEALTH_STATE = "com.louis.app.cavity.WORK_DATA_HEALTH_STATE"
    }

}
