package com.louis.app.cavity.ui.addbottle.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.HistoryEntryType
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class OtherInfoViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _updatedBottle = MutableLiveData<Bottle>()
    val updatedBottle: LiveData<Bottle>
        get() = _updatedBottle

    private val _bottleUpdatedEvent = MutableLiveData<Event<Int>>()
    val bottleUpdatedEvent: LiveData<Event<Int>>
        get() = _bottleUpdatedEvent

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private var bottleId = 0L
    private var pdfPath: String = ""

    val hasPdf: Boolean
        get() = pdfPath.isNotBlank()

    fun start(editedBottleId: Long) {
        if (editedBottleId != 0L) {
            bottleId = editedBottleId
            triggerEditMode(editedBottleId)
        } else {
            _updatedBottle.postValue(null)
        }
    }

    fun setPdfPath(path: String) {
        pdfPath = path
    }

    fun saveBottle(otherInfo: String, addToFavorite: Boolean, friendId: Long?) {
        if (bottleId == 0L) {
            _userFeedback.postOnce(R.string.base_error)
            return
        }

        viewModelScope.launch(IO) {
            val step1 = repository.getBottleByIdNotLive(bottleId)
            val bottle = mergeStep1Bottle(step1, addToFavorite, otherInfo)

            repository.updateBottle(bottle)
            addHistoryEntry(bottle.buyDate, friendId)

            _updatedBottle.postValue(null)
            _bottleUpdatedEvent.postOnce(R.string.bottle_added)
        }
    }

    fun getAllFriends() = repository.getAllFriends()

    fun insertFriend(nameLastName: String) {
        viewModelScope.launch(IO) {
            try {
                repository.insertFriend(Friend(0, nameLastName, ""))
                _userFeedback.postOnce(R.string.friend_added)
            } catch (e: IllegalArgumentException) {
                _userFeedback.postOnce(R.string.input_error)
            } catch (e: SQLiteConstraintException) {
                _userFeedback.postOnce(R.string.friend_already_exists)
            }
        }
    }

    private suspend fun addHistoryEntry(buyDate: Long, friendId: Long?) {
        if (friendId == null) {
            val historyEntry =
                HistoryEntry(0, buyDate, bottleId, null, "", HistoryEntryType.TYPE_REPLENISHMENT, 0)
            repository.insertHistoryEntry(historyEntry)
        } else {
            val historyEntry =
                HistoryEntry(0, buyDate, bottleId, null, "", HistoryEntryType.TYPE_GIFTED_BY, 0)
            repository.declareGiftedBottle(historyEntry, friendId)
        }
    }

    private fun triggerEditMode(bottleId: Long) {
        viewModelScope.launch(IO) {
            val editedBottle = repository.getBottleByIdNotLive(bottleId)
            _updatedBottle.postValue(editedBottle)
        }
    }

    // Hiding boring stuff
    private fun mergeStep1Bottle(step1: Bottle, addToFavorite: Boolean, otherInfo: String): Bottle {
        return Bottle(
            step1.id,
            step1.wineId,
            step1.vintage,
            step1.apogee,
            addToFavorite.toInt(),
            step1.count,
            step1.price,
            step1.currency,
            otherInfo,
            step1.buyLocation,
            step1.buyDate,
            step1.tasteComment,
            pdfPath,
            step1.consumed
        )
    }
}
