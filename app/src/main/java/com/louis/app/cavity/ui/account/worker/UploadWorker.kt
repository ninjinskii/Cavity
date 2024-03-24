package com.louis.app.cavity.ui.account.worker

import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.louis.app.cavity.db.AccountRepository
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.domain.backup.BackupBuilder
import io.sentry.Sentry

class UploadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val repository = WineRepository.getInstance(context as Application)
    private val accountRepository = AccountRepository.getInstance(context as Application)
    private val backupBuilder = BackupBuilder(context)

    override suspend fun doWork(): Result {
        return try {
            backupBuilder.backup(accountRepository, repository)
            Result.success()
        } catch (e: BackupBuilder.UncompleteExportException) {
            if (runAttemptCount < 1) {
                Result.retry()
            } else {
                Sentry.captureException(e)
                Result.failure()
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure()
        }
    }

    companion object {
        const val WORK_TAG = "com.louis.app.cavity.upload-db"
    }
}
