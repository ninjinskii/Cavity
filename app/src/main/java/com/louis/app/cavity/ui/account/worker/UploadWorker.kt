package com.louis.app.cavity.ui.account.worker

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.louis.app.cavity.db.AccountRepository
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.FileAssoc
import com.louis.app.cavity.network.response.ApiResponse
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UploadWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    private val repository = WineRepository.getInstance(context as Application)
    private val accountRepository = AccountRepository.getInstance(context as Application)

    override suspend fun doWork(): Result {
        return try {
            uploadDatabase()
            Result.success()
        } catch (e: UncompleteExportException) {
            if (runAttemptCount < 1) {
                Result.retry()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private suspend fun uploadDatabase() = withContext(IO) {
        with(accountRepository) {
            launch {
                val wines = repository.getAllWinesNotLive()
                val bottles = repository.getAllBottlesNotLive()

                listOf(
                    postCounties(repository.getAllCountiesNotLive()),
                    postWines(wines),
                    postBottles(bottles),
                    postFriends(repository.getAllFriendsNotLive()),
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

                copyFilesToExternalDirectory(wines + bottles)
            }
        }
    }

    private fun copyFilesToExternalDirectory(fileAssocs: List<FileAssoc>) {
        fileAssocs.forEach {
            val uriString = it.getFilePath()
            val uri = Uri.parse(uriString)

            FileProcessor(context, uri).run {
                copyToExternalDir(it)
            }
        }
    }

    class UncompleteExportException : Exception()

    companion object {
        const val WORK_TAG = "com.louis.app.cavity.upload-db"
    }
}
