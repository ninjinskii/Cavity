package com.louis.app.cavity.ui.account.worker

import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.louis.app.cavity.db.AccountRepository
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.network.response.ApiResponse
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext


class DownloadWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    private val repository = WineRepository.getInstance(context as Application)
    private val accountRepository = AccountRepository.getInstance(context as Application)

    override suspend fun doWork(): Result {
        return try {
            downloadDatabase()
            Result.success()
        } catch (e: UncompleteImportException) {
            if (runAttemptCount < 1) {
                Result.retry()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun downloadDatabase() = withContext(IO) {
        with(repository) {
            transaction {
                deleteAllCounties()
                deleteAllWines()
                deleteAllBottles()
                deleteAllFriends()
                deleteAllGrapes()
                deleteAllReviews()
                deleteAllHistoryEntries()
                deleteAllTastings()
                deleteAllTastingActions()
                deleteAllFReviews()
                deleteAllQGrapes()
                deleteAllFriendHistoryXRefs()
                deleteAllTastingFriendXRefs()

                insertCounties(validateResponse(accountRepository.getCounties()))
                insertWines(validateResponse(accountRepository.getWines()))
                insertBottles(validateResponse(accountRepository.getBottles()))
                insertFriends(validateResponse(accountRepository.getFriends()))
                insertGrapes(validateResponse(accountRepository.getGrapes()))
                insertReviews(validateResponse(accountRepository.getReviews()))
                insertHistoryEntries(validateResponse(accountRepository.getHistoryEntries()))
                insertTastings(validateResponse(accountRepository.getTastings()))
                insertTastingActions(validateResponse(accountRepository.getTastingActions()))
                insertFilledReviews(validateResponse(accountRepository.getFReviews()))
                insertQGrapes(validateResponse(accountRepository.getQGrapes()))
                insertFriendHistoryXRefs(validateResponse(accountRepository.getHistoryXFriend()))
                insertTastingFriendXRefs(validateResponse(accountRepository.getTastingXFriend()))
            }
        }
    }

    private fun <T> validateResponse(response: ApiResponse<List<T>>): List<T> {
        if (response is ApiResponse.Success) {
            return response.value
        }

        throw UncompleteImportException()
    }

    class UncompleteImportException : Exception()

    companion object {
        const val WORK_TAG = "com.louis.app.cavity.download-db"
    }
}
