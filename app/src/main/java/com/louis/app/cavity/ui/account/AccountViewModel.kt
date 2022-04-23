package com.louis.app.cavity.ui.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.louis.app.cavity.db.AccountRepository
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.ui.account.worker.UploadWorker
import java.util.concurrent.TimeUnit

class AccountViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)
    private val accountRepository = AccountRepository.getInstance(app)
    private val workManager = WorkManager.getInstance(app)

    fun export() {
        OneTimeWorkRequestBuilder<UploadWorker>()
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS, // 10 sec
                TimeUnit.MILLISECONDS
            )
            .build().let {
                workManager.enqueue(it)
            }
    }
}
