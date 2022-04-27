package com.louis.app.cavity.ui.account.worker

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.webkit.MimeTypeMap
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.louis.app.cavity.db.AccountRepository
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.FileAssoc
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.network.response.ApiResponse
import com.louis.app.cavity.network.response.FileTransfer
import com.louis.app.cavity.ui.Cavity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*


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
            Result.failure()
        }
    }

    private suspend fun uploadDatabase() = withContext(IO) {
        with(accountRepository) {
            launch {
                val wines = repository.getAllWinesNotLive()

                listOf(
                    postCounties(repository.getAllCountiesNotLive()),
                    postWines(wines),
                    postBottles(repository.getAllBottlesNotLive()),
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

                uploadFiles(wines)
            }
        }
    }

    private suspend fun uploadFiles(wines: List<FileAssoc>) {
        wines
            .filter { it.getFilePath().isNotBlank() }
            .forEach {
                val isContentProvider = it.getFilePath().startsWith("content://")

                if (isContentProvider) {
                    migrateFileToExternalDirectory(it)
                }

                val uri = Uri.parse(it.getFilePath())
                convertImageToBase64(uri)?.let { fileT ->
                    when (it) {
                        is Wine -> accountRepository.postWineImage(it, fileT)
                    }
                }
            }
    }

    // We're in IO context, so we'are not worried about a blockin call
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun convertImageToBase64(uri: Uri) = withContext(IO) {
        try {
            val fileInputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(fileInputStream)
            val extension =
                MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(context.contentResolver.getType(uri))
                    ?: uri.path?.substringAfterLast(".", "")
                    ?: throw FileNotFoundException()

            val compressFormat = when (extension) {
                PNG_FORMAT -> Bitmap.CompressFormat.PNG
                else -> Bitmap.CompressFormat.JPEG
            }

            val baos = ByteArrayOutputStream()
            bitmap.compress(compressFormat, 100, baos)

            return@withContext baos.use {
                val base64 = Base64.encodeToString(it.toByteArray(), Base64.DEFAULT)
                FileTransfer(extension, base64)
            }
        } catch (e: FileNotFoundException) {
            return@withContext null
        }
    }

    // We're in IO context, so we'are not worried about a blocking call
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun migrateFileToExternalDirectory(fileAssoc: FileAssoc) = withContext(IO) {
        try {
            val uriString = fileAssoc.getFilePath()
            val uri = Uri.parse(uriString)
            val fileInputStream = context.contentResolver.openInputStream(uri)
            val externalDir = context.getExternalFilesDir(null)!!.path
            val filename = fileAssoc.getFileName()
            val directory = fileAssoc.getDirectory()
            val outputFile = File("$externalDir${directory}/${filename}.${JPEG_FORMAT}")
            val tempDir = File("$externalDir${Cavity.PHOTOS_DIRECTORY}")
            val compressFormat = when (uriString.substringAfterLast(".", "")) {
                PNG_FORMAT -> Bitmap.CompressFormat.PNG
                else -> Bitmap.CompressFormat.JPEG
            }

            if (!tempDir.exists()) {
                tempDir.mkdir()
            }

            if (!outputFile.exists()) {
                outputFile.createNewFile()
            }

            val bitmap = BitmapFactory.decodeStream(fileInputStream)
            val baos = ByteArrayOutputStream()
            bitmap.compress(compressFormat, 100, baos)

            FileOutputStream(outputFile, false).use {
                it.write(baos.toByteArray())
            }
        } catch (e: IOException) {
            // Do nothing
        } catch (e: FileNotFoundException) {
            // Do nothing
        }
    }

    class UncompleteExportException : Exception()

    companion object {
        const val WORK_TAG = "upload-db"
        const val PNG_FORMAT = "png"
        const val JPEG_FORMAT = "jpeg"
    }
}
