package com.louis.app.cavity.ui.account.worker

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.webkit.MimeTypeMap
import com.louis.app.cavity.model.FileAssoc
import java.io.*

class FileProcessor(private val context: Context, private val uri: Uri) {
    val extension: String?
        get() = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(context.contentResolver.getType(uri))
            ?: uri.path?.substringAfterLast(".", "")

    private val inputStream: InputStream?
        get() = context.contentResolver.openInputStream(uri)

    fun copyToExternalDir(fileAssoc: FileAssoc) {
        try {
            val externalDir = context.getExternalFilesDir(null)!!.path
            val filename = fileAssoc.getExternalFileName()
            val directory = fileAssoc.getExternalSubDirectory()
            val outputFile = File("$externalDir$directory/${filename}.$extension")
            val subDir = File("$externalDir$directory")
//            val compressFormat = when (extension) {
//                UploadWorker.PNG_FORMAT -> Bitmap.CompressFormat.PNG
//                else -> Bitmap.CompressFormat.JPEG
//            }

            if (!subDir.exists()) {
                subDir.mkdir()
            }

            if (!outputFile.exists()) {
                outputFile.createNewFile()
            }

            FileOutputStream(outputFile, false).use {
                inputStream.use { inputStream ->
                    inputStream?.copyTo(it)
                }
            }

//            inputStream.use {
//                val bitmap = BitmapFactory.decodeStream(it)
//                val baos = ByteArrayOutputStream()
//                bitmap.compress(compressFormat, 100, baos)
//
//                FileOutputStream(outputFile, false).use { outputStream ->
//                    outputStream.write(baos.toByteArray())
//                }
//            }
        } catch (e: IOException) {
            // Do nothing
        } catch (e: FileNotFoundException) {
            // Do nothing
        }
    }

    fun getBase64(): String? {
        return ByteArrayOutputStream().use {
            inputStream.use { inputStream ->
                inputStream?.copyTo(it)?.run {
                    Base64.encodeToString(it.toByteArray(), Base64.DEFAULT)
                }
            }
        }
    }
}
