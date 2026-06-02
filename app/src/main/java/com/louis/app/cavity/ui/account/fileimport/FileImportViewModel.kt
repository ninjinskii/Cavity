package com.louis.app.cavity.ui.account.fileimport

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.domain.error.SentryErrorReporter
import com.louis.app.cavity.domain.fileimport.BottleBinder
import com.louis.app.cavity.domain.fileimport.FileBinder
import com.louis.app.cavity.domain.fileimport.FriendBinder
import com.louis.app.cavity.domain.fileimport.WineBinder
import com.louis.app.cavity.ui.BaseViewModel
import kotlinx.coroutines.launch
import java.io.File

sealed interface FileImportEvent {
    data class FileImported(val binded: Int, val total: Int) : FileImportEvent
}

data class FileImportUiState(val placeholder: Unit = Unit)

class FileImportViewModel(private val app: Application) : BaseViewModel<FileImportUiState, FileImportEvent>(app, FileImportUiState()) {
    private val errorReporter = SentryErrorReporter.getInstance(app)

    fun bindFiles(uris: List<Uri>, contentResolver: ContentResolver) {
        val total = uris.size
        var binded = 0

        viewModelScope.launch {
            for (uri in uris) {
                val binder = binderFactory(uri, contentResolver)

                if (binder != null) {
                    try {
                        // Might throw NPE even if kotlin thinks differently
                        binder.bind(app, uri)
                        binded++
                    } catch (e: NullPointerException) {
                        errorReporter.captureMessage(
                            "File import: NPE when retieving id from filename"
                        )
                    } catch (e: NumberFormatException) {
                        errorReporter.captureMessage(
                            "File import: NumberFormatException when retrieving id from filename"
                        )
                    }
                }
            }

            emitEvent(FileImportEvent.FileImported(binded, total))
        }
    }

    private fun binderFactory(uri: Uri, contentResolver: ContentResolver): FileBinder? {
        val filename = getFileName(uri, contentResolver)
        val split = filename?.split(".")

        // Weird file name. Don't bother.
        if (filename == null || split == null || split.size != 2) {
            return null
        }

        val extension = split.last()
        val isFriend = filename.matches(Regex(".*-f\\d*\\..*"))
        val name = filename.split(".").first()

        return when {
            extension == "pdf" -> BottleBinder(name)
            isFriend -> FriendBinder(name)
            else -> WineBinder(name)
        }
    }

    private fun getFileName(uri: Uri, contentResolver: ContentResolver): String? =
        when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> getContentFileName(uri, contentResolver)
            else -> uri.path?.let(::File)?.name
        }

    private fun getContentFileName(uri: Uri, contentResolver: ContentResolver): String? =
        runCatching {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
                return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                    .let(cursor::getString)
            }
        }.getOrNull()
}
