package com.louis.app.cavity.ui.account.fileimport

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.louis.app.cavity.R
import com.louis.app.cavity.db.AccountRepository
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.network.response.ApiResponse
import com.louis.app.cavity.ui.account.worker.DownloadWorker
import com.louis.app.cavity.ui.account.worker.UploadWorker
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toBoolean
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class FileImportViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _fileImportedEvent = MutableLiveData<Event<Pair<Int, Int>>>()
    val fileImportedEvent: LiveData<Event<Pair<Int, Int>>>
        get() = _fileImportedEvent

    fun bindFiles(uris: List<Uri>) {
        val total = uris.size
        var binded = 0

        viewModelScope.launch {
            for (uri in uris) {
                val binder = binderFactory(uri)

                if (binder != null) {
                    binder.bind(repository, uri)
                    binded++
                }
            }

            _fileImportedEvent.postOnce(binded to total)
        }
    }

    private fun binderFactory(uri: Uri): FileBinder? {
        val filename = uri.lastPathSegment ?: return null
        L.v(filename)
        val split = filename.split(".")

        // Weird file name. Dont bother.
        if (split.size != 2) {
            return null
        }

        val extension = split.last()
        val name = split.first()
        val isFriend = name.matches(Regex("-f[0-9]\b"))

        return when {
            extension == "pdf" -> BottleBinder()
            isFriend -> FriendBinder()
            else -> WineBinder()
        }
    }
}
