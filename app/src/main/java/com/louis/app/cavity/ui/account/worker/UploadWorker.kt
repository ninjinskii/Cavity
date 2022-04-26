package com.louis.app.cavity.ui.account.worker

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.louis.app.cavity.db.AccountRepository
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.FileAssoc
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.network.response.ApiResponse
import com.louis.app.cavity.network.response.FileTransfer
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


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

                val fileTransfert = convertImageToBase64(it.getFilePath().split(":")[1])

                when (it) {
                    is Wine -> accountRepository.postWineImage(it, fileTransfert)
                }
            }
    }

    // We're in IO context, so we'are not worried about a blockin call
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun convertImageToBase64(path: String) = withContext(IO) {
        val bitmap = BitmapFactory.decodeFile(Uri.parse(path).toString())
        val extension = path.substringAfterLast(".", "")
        val compressFormat = when (extension) {
            PNG_FORMAT -> Bitmap.CompressFormat.PNG
            else -> Bitmap.CompressFormat.JPEG
        }

        val baos = ByteArrayOutputStream()
        bitmap.compress(compressFormat, 100, baos)

        return@withContext baos.toByteArray().let {
            val base64 = Base64.encodeToString(it, Base64.DEFAULT)
            withContext(IO) {
                baos.close()
            }
            FileTransfer(extension, base64)
        }
    }

    // We're in IO context, so we'are not worried about a blocking call
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun migrateFileToExternalDirectory(fileAssoc: FileAssoc) = withContext(IO) {
        val path =
            fileAssoc.getFilePath() // path = "content://com.android.providers.downloads.documents/document/msf%3A24"
        val uri = Uri.parse(path)
        val inputFile = context.contentResolver.openInputStream(uri)
        val externalDir = context.getExternalFilesDir(null)!!.path
        val filename = fileAssoc.getFileName()
        val directory = fileAssoc.getDirectory()
        val outputFile = File("$externalDir${directory}/${filename}.jpeg") // TODO: ext

        if (!outputFile.exists()) {
            outputFile.createNewFile()
        }

        val bitmap = BitmapFactory.decodeStream(inputFile)
//        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
//        } else {
//            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
//        }

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val fos = FileOutputStream(outputFile, false)
        fos.use {
            it.write(baos.toByteArray())
        }
    }

    class UncompleteExportException : Exception()

    companion object {
        const val WORK_TAG = "upload-db"
        const val PNG_FORMAT = "png"
    }
}
