package com.louis.app.cavity.ui.account.worker

import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.louis.app.cavity.db.AccountRepository
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.util.L
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UploadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val repository = WineRepository.getInstance(context as Application)
    private val accountRepository = AccountRepository.getInstance(context as Application)

    override suspend fun doWork(): Result {
        return try {
            uploadDatabase()
            Result.success()
        } catch (e: Exception) {
            L.e(e)
            Result.failure()
        }
    }

    private suspend fun uploadDatabase() = withContext(IO) {
        with(accountRepository) {
            listOf(
                launch { postCounties(repository.getAllCountiesNotLive()) },
                launch { postWines(repository.getAllWinesNotLive()) },
                launch { postBottles(repository.getAllBottlesNotLive()) },
                launch { postFriends(repository.getAllFriendsNotLive()) },
                launch { postGrapes(repository.getAllGrapesNotLive()) },
                launch { postReviews(repository.getAllReviewsNotLive()) },
                launch { postHistoryEntries(repository.getAllEntriesNotPagedNotLive()) },
                launch { postTastings(repository.getAllTastingsNotLive()) },
                launch { postTastingActions(repository.getAllTastingActionsNotLive()) },
                launch { postFReviews(repository.getAllFReviewsNotLive()) },
                launch { postQGrapes(repository.getAllQGrapesNotLive()) },
                launch { postTastingFriendsXRefs(repository.getAllTastingXFriendsNotLive()) },
                launch { postHistoryFriendsXRefs(repository.getAllHistoryXFriendsNotLive()) }
            ).joinAll()
        }
    }
}
