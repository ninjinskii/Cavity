package com.louis.app.cavity.ui.account.worker

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.louis.app.cavity.db.AccountRepository
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.FileAssoc
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.network.response.ApiResponse
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class DownloadWorker(private val context: Context, params: WorkerParameters) :
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

    private suspend fun downloadFiles(fileAssocs: List<FileAssoc>) {
        fileAssocs
            .filter { it.getFilePath().isNotBlank() }
            .forEach {
                val ftResponse = when (it) {
                    is Wine -> accountRepository.getWineImage(it)
                    else /* is Bottle */ -> accountRepository.getBottlePdf(it as Bottle)
                }

                if (ftResponse is ApiResponse.Success) {
                    val ft = ftResponse.value
                    val externalDir = context.getExternalFilesDir(null)!!.path
                    val filename = it.getExternalFileName()
                    val directory = it.getExternalSubDirectory()
                    val outputFile = File("$externalDir$directory/${filename}.${ft.extension}")
                    val subDir = File("$externalDir$directory")

                    try {
                        if (!subDir.exists()) {
                            subDir.mkdir()
                        }

                        if (!outputFile.exists()) {
                            outputFile.createNewFile()
                        }

                        ExifInterface(outputFile).let { a ->
                            val orientation = a.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL
                            )
                            val degrees = when (orientation) {
                                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                                else -> 0
                            }

                            FileOutputStream(outputFile, false).use { outputStream ->
                                val matrix = Matrix() //
                                matrix.postRotate(degrees.toFloat()) //
                                val bytes = Base64.decode(ft.content, Base64.DEFAULT)
                                val b = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) //
                                val b2 =
                                    Bitmap.createBitmap(b, 0, 0, b.width, b.height, matrix, true) //
                                b2.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                                //outputStream.write(bytes)
                            }
                        }
                    } catch (e: FileNotFoundException) {
                        // Do nothing
                    } catch (e: IOException) {
                        // Do nothing
                    }

                    // do the mapping on wine and bottles object to new file location
                    // take care of image orientation (voir favoris)
                }
            }
    }

    class UncompleteImportException : Exception()

    companion object {
        const val WORK_TAG = "com.louis.app.cavity.download-db"
    }
}
