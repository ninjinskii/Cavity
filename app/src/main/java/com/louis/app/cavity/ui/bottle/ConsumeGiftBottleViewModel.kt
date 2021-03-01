package com.louis.app.cavity.ui.bottle

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.HistoryEntryType
import com.louis.app.cavity.model.relation.crossref.FriendHistoryEntryXRef
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ConsumeGiftBottleViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    var date: Long = System.currentTimeMillis()

    fun consumeBottle(bottleId: Long, comment: String, friends: List<Long>) {
        val historyEntry =
            HistoryEntry(0, date, bottleId, null, comment, HistoryEntryType.TYPE_CONSUME, 0)

        viewModelScope.launch(IO) {
            repository.insertHistoryEntryAndFriends(historyEntry, friends)
        }
    }

    fun giftBottle(bottleId: Long, comment: String, friendId: Long) {
        val historyEntry =
            HistoryEntry(0, date, bottleId, null, comment, HistoryEntryType.TYPE_GIFTED_TO, 0)

        viewModelScope.launch(IO) {
            repository.insertHistoryEntryAndFriends(historyEntry, listOf(friendId))
        }
    }

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

    suspend fun getAllFriendsNotLive() = repository.getAllFriendsNotLive()
}
