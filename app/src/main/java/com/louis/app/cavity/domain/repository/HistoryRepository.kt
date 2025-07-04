package com.louis.app.cavity.domain.repository

import android.app.Application
import androidx.room.withTransaction
import com.louis.app.cavity.domain.history.isConsumption
import com.louis.app.cavity.domain.history.isReplenishment
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.HistoryXFriend

class HistoryRepository private constructor(app: Application) : Repository(app) {
    companion object {
        @Volatile
        var instance: HistoryRepository? = null

        fun getInstance(app: Application) =
            instance ?: synchronized(this) {
                instance ?: HistoryRepository(app).also { instance = it }
            }
    }

    private val historyDao = database.historyDao()
    private val historyXFriendDao = database.historyXFriendDao()

    suspend fun updateEntry(entry: HistoryEntry) = historyDao.updateEntry(entry)

    fun getAllEntries() = historyDao.getAllEntries()

    suspend fun getAllEntriesNotPagedNotLive() = historyDao.getAllEntriesNotPagedNotLive()

    fun getYears() = historyDao.getYears()

    fun getEntriesByType(type1: Int, type2: Int) = historyDao.getEntriesByType(type1, type2)

    fun getEntriesForWine(wineId: Long) = historyDao.getEntriesForWine(wineId)

    fun getEntriesForBottle(bottleId: Long) = historyDao.getEntriesForBottle(bottleId)

    fun getReplenishmentForBottleNotPaged(bottleId: Long) =
        historyDao.getReplenishmentForBottleNotPaged(bottleId)

    fun getReplenishmentForBottleNotPagedNotLive(bottleId: Long) =
        historyDao.getReplenishmentForBottleNotPagedNotLive(bottleId)

    fun getEntriesForDate(date: Long) = historyDao.getEntriesForDate(date)

    fun getFavoriteEntries() = historyDao.getFavoriteEntries()

    suspend fun getAllHistoryXFriendsNotLive() = historyXFriendDao.getAllHistoryXFriendsNotLive()

    suspend fun deleteAllFriendHistoryXRefs() = historyXFriendDao.deleteAll()

    suspend fun insertHistoryEntries(entries: List<HistoryEntry>) =
        historyDao.insertEntries(entries)

    suspend fun insertFriendHistoryXRefs(fxh: List<HistoryXFriend>) =
        historyXFriendDao.insertHistoryXFriends(fxh)

    suspend fun clearReplenishmentsForBottle(bottleId: Long) =
        historyDao.clearReplenishmentsForBottle(bottleId)

    suspend fun insertHistoryXFriend(xref: HistoryXFriend) =
        historyXFriendDao.insertHistoryXFriend(xref)

    suspend fun insertHistoryEntry(entry: HistoryEntry): Long {
        return database.withTransaction {
            ensureEntryTypeUnicityForBottle(entry)
            historyDao.insertEntry(entry)
        }
    }

    // TODO: service ?
    suspend fun insertHistoryEntry(entry: HistoryEntry, friends: List<Long>) {
        database.withTransaction {
            val entryId = insertHistoryEntry(entry)
            val historyXFriends = friends.map { HistoryXFriend(entryId, it) }
            historyXFriendDao.insertHistoryXFriends(historyXFriends)
        }
    }

    // TODO: note to myself: this method will be removed, since in the future multiple friends can give a unique bottle
    suspend fun insertGiftedReplenishment(entry: HistoryEntry, friendId: Long) {
        database.withTransaction {
            historyDao.clearReplenishmentsForBottle(entry.bottleId)

            val entryId = historyDao.insertEntry(entry)

            historyXFriendDao.insertHistoryXFriend(
                HistoryXFriend(entryId, friendId)
            )
        }
    }

    suspend fun deleteAllHistoryEntries() = historyDao.deleteAll()

    private suspend fun ensureEntryTypeUnicityForBottle(entry: HistoryEntry) {
        when {
            entry.type.isReplenishment() ->
                historyDao.clearReplenishmentsForBottle(entry.bottleId)

            entry.type.isConsumption() ->
                historyDao.clearConsumptionsForBottle(entry.bottleId)
        }
    }
}
