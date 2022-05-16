package com.louis.app.cavity.ui.account.worker

import android.content.Context
import android.net.Uri
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
            val externalPath = context.getExternalFilesDir(null)!!.path
            val extarnalFilename = fileAssoc.getExternalFilename()
            val externalSubPath = fileAssoc.getExternalSubPath()
            val subDir = File("$externalPath$externalSubPath")
            val outputFile = File("$externalPath$externalSubPath/${extarnalFilename}.$extension")

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
        } catch (e: IOException) {
            // Do nothing
        } catch (e: FileNotFoundException) {
            // Do nothing
        }
    }
}
