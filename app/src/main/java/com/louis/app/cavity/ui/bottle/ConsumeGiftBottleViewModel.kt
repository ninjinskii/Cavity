package com.louis.app.cavity.ui.bottle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.relation.crossref.FriendHistoryEntryXRef
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ConsumeGiftBottleViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)
    private val consume = 0
    private val giftTo = 2
    var date: Long = System.currentTimeMillis()

    fun consumeBottle(bottleId: Long, comment: String, friends: List<Long>) {
        val historyEntry = HistoryEntry(0, date, bottleId, null, comment, type = consume)

        viewModelScope.launch(IO) {
            val historyId = repository.insertHistoryEntry(historyEntry)
            val historyXFriends = friends.map { FriendHistoryEntryXRef(historyId, it) }

            repository.insertFriendHistoryXRef(historyXFriends)
        }
    }

    fun giftBottle(bottleId: Long, comment: String, friends: List<Long>) {
        val historyEntry = HistoryEntry(0, date, bottleId, null, comment, type = giftTo)

        viewModelScope.launch(IO) {
            val historyId = repository.insertHistoryEntry(historyEntry)
            val
        }
    }

    suspend fun getAllFriendsNotLive() = repository.getAllFriendsNotLive()
}
