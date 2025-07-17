package com.louis.app.cavity.ui.bottle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.dao.BoundedBottle
import com.louis.app.cavity.domain.history.HistoryEntryType
import com.louis.app.cavity.domain.history.isConsumption
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.FriendRepository
import com.louis.app.cavity.domain.repository.HistoryRepository
import com.louis.app.cavity.model.HistoryEntry
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ConsumeGiftBottleViewModel(app: Application) : AndroidViewModel(app) {
    private val bottleRepository = BottleRepository.getInstance(app)
    private val historyRepository = HistoryRepository.getInstance(app)
    private val friendRepository = FriendRepository.getInstance(app)

    var date: Long = System.currentTimeMillis()

    fun consumeBottle(
        bottleId: Long,
        comment: String,
        friends: List<Long>,
        date: Long,
        isAGift: Boolean,
        isTasting: Boolean = false
    ) {
        val type = when {
            isTasting -> HistoryEntryType.TASTING
            isAGift -> HistoryEntryType.GIFTED_TO
            else -> HistoryEntryType.REMOVE
        }
        val historyEntry = HistoryEntry(0, date, bottleId, null, comment, type, 0)

        viewModelScope.launch(IO) {
            bottleRepository.transaction {
                bottleRepository.consumeBottle(bottleId)
                bottleRepository.removeTastingForBottle(bottleId)
                historyRepository.insertHistoryEntry(historyEntry, friends)
            }
        }
    }

    /**
     * Note: this method work only if boundedBottle has already been consumed once in the past
     * The point of it is to cancel a consumption cancelling so we need to get bottle associated infos.
     */
    fun consumeBottle(boundedBottle: BoundedBottle) {
        val (bottle, _, _, _, historyEntryWithFriends) = boundedBottle
        val consumption = historyEntryWithFriends.find { entryWithFriends ->
            entryWithFriends.historyEntry.type.isConsumption()
        }

        val consumptionEntry = consumption?.historyEntry ?: return
        val consumptionFriend = consumption.friends.map { it.id }
        val isAGift = consumptionEntry.type == HistoryEntryType.GIFTED_TO
        val comment = consumptionEntry.comment
        val date = consumptionEntry.date
        val isTasting = bottle.tastingId != null

        consumeBottle(bottle.id, comment, consumptionFriend, date, isAGift, isTasting)
    }

    fun getAllFriends() = friendRepository.getAllFriends()
}
