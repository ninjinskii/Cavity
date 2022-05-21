package com.louis.app.cavity.ui.account.worker

import android.app.Application
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.FileAssoc
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.util.L
import java.io.*

class FileProcessor(private val context: Context, private val fileAssoc: FileAssoc) {
    private val repository = WineRepository.getInstance(context.applicationContext as Application)
    private val uri = Uri.parse(fileAssoc.getFilePath())
    private val externalPath = context.getExternalFilesDir(null)!!.path
    private val externalSubPath = fileAssoc.getExternalSubPath()
    private val externalFilename = fileAssoc.getExternalFilename()
    private val outputFile = File("$externalPath$externalSubPath/${externalFilename}.$extension")
    private val outputPath = "file:///${outputFile.path}"

    private val extension: String?
        get() = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(context.contentResolver.getType(uri))
            ?: uri.path?.substringAfterLast(".", "")

    private val inputStream: InputStream?
        get() = context.contentResolver.openInputStream(uri)

    fun copyToExternalDir() {
        try {
            val subDir = File("$externalPath$externalSubPath")

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

    suspend fun updateFilePath() {
        L.v("updating file path")
        L.v(outputPath)
        L.v(Uri.parse(outputPath).toString())
        when (fileAssoc) {
            is Wine -> repository.updateWine(fileAssoc.copy(imgPath = outputPath))
            is Bottle -> repository.updateBottle(fileAssoc.copy(pdfPath = outputPath))
        }
    }
}
