package com.louis.app.cavity.ui.bottle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.relation.crossref.FriendHistoryEntryXRef
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class UseBottleViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)
    private val use = 0
    var date: Long = System.currentTimeMillis()

    fun useBottle(bottleId: Long, friends: List<Long>) {
        val historyEntry = HistoryEntry(0, date, bottleId, null, type = use)

        viewModelScope.launch(IO) {
            val historyId = repository.insertHistoryEntry(historyEntry)
            val historyXFriends = friends.map { FriendHistoryEntryXRef(historyId, it) }

            repository.insertFriendHistoryXRef(historyXFriends)
        }
    }

    suspend fun getAllFriendsNotLive() = repository.getAllFriendsNotLive()
}
