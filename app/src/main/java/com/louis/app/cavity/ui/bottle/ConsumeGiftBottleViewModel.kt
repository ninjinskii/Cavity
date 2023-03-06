package com.louis.app.cavity.ui.bottle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.BoundedBottle
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.HistoryEntry
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ConsumeGiftBottleViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    var date: Long = System.currentTimeMillis()

    fun consumeBottle(
        bottleId: Long,
        comment: String,
        friends: List<Friend>,
        date: Long,
        isAGift: Boolean,
        isTasting: Boolean = false
    ) {
        if (isAGift && friends.size != 1) {
            throw IllegalArgumentException("Trying to gift a bottle without/with too many friends")
        }

        val friendIds = friends.map { it.id }
        val typeConsume = 0
        val typeGiftTo = 2
        val typeTasting = 4
        val type = when {
            isTasting -> typeTasting
            isAGift -> typeGiftTo
            else -> typeConsume
        }
        val historyEntry = HistoryEntry(0, date, bottleId, null, comment, type, 0)

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

    fun consumeBottle(boundedBottle: BoundedBottle) {
        val (bottle, _, _, _, historyEntryWithFriends) = boundedBottle
        val consumptionType = listOf(0, 2, 4)
        val consumption = historyEntryWithFriends.find { entryWithFriends ->
            entryWithFriends.historyEntry.type in consumptionType
        }

        val consumptionEntry = consumption?.historyEntry ?: return
        val consumptionFriend = consumption.friends
        val isAGift = consumptionEntry.type == 2
        val comment = consumptionEntry.comment
        val date = consumptionEntry.date
        val isTasting = bottle.tastingId != null

        consumeBottle(bottle.id, comment, consumptionFriend, date, isAGift, isTasting)
    }

    fun getAllFriends() = repository.getAllFriends()
}
