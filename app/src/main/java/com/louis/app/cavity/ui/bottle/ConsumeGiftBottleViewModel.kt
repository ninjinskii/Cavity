package com.louis.app.cavity.ui.bottle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.HistoryEntry
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ConsumeGiftBottleViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    var date: Long = System.currentTimeMillis()

    fun consumeBottle(bottleId: Long, comment: String, friends: List<Friend>) {
        val typeConsume = 0
        val friendIds = friends.map { it.id }
        val historyEntry = HistoryEntry(0, date, bottleId, null, comment, typeConsume, 0)

        viewModelScope.launch(IO) {
            repository.run {
                transaction {
                    consumeBottle(bottleId)
                    removeTastingForBottle(bottleId)
                    insertHistoryEntryAndFriends(historyEntry, friendIds)
                }
            }
        }
    }

    fun giftBottle(bottleId: Long, comment: String, friendId: Long) {
        val typeGiftTo = 2
        val historyEntry = HistoryEntry(0, date, bottleId, null, comment, typeGiftTo, 0)

        viewModelScope.launch(IO) {
            repository.run {
                transaction {
                    consumeBottle(bottleId)
                    removeTastingForBottle(bottleId)
                    insertHistoryEntryAndFriends(historyEntry, listOf(friendId))
                }
            }
        }
    }

    fun getAllFriends() = repository.getAllFriends()
}
