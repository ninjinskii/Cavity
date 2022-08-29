package com.louis.app.cavity.ui.account.fileimport

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.launch

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
                    try {
                        binder.bind(repository, uri) // Might throw NPE even if kotlin thinks differently
                        binded++
                    } catch (e: NullPointerException) {
                        // Do nothing
                    } catch (e: NumberFormatException) {
                        // Cannot extract id from filename.
                        // Do nothing
                    }
                }
            }

            _fileImportedEvent.postOnce(binded to total)
        }
    }

    private fun binderFactory(uri: Uri): FileBinder? {
        val filename = uri.lastPathSegment.toString()
        val split = filename.split(".")

        // Weird file name. Dont bother.
        if (split.size != 2) {
            return null
        }

        val extension = split.last()
        val name = split.first()
        val isFriend = name.matches(Regex("-f[0-9]*\b"))

        return when {
            extension == "pdf" -> BottleBinder()
            isFriend -> FriendBinder()
            else -> WineBinder()
        }
    }
}
