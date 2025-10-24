package com.louis.app.cavity.domain.repository

import android.app.Application
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.room.withTransaction
import com.louis.app.cavity.R
import com.louis.app.cavity.db.dao.BoundedHistoryEntry
import com.louis.app.cavity.domain.history.isConsumption
import com.louis.app.cavity.domain.history.isReplenishment
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.HistoryXFriend
import com.louis.app.cavity.ui.history.HistoryFilter
import com.louis.app.cavity.ui.history.HistoryUiModel
import com.louis.app.cavity.util.DateFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    fun getPagedEntriesFilteredBy(filter: HistoryFilter): Flow<PagingData<HistoryUiModel>> {
        return Pager(
            PagingConfig(pageSize = 50, prefetchDistance = 20, enablePlaceholders = true)
        ) {
            getDataSource(filter)
        }.flow
            .map { pagingData ->
                pagingData
                    .map { model -> HistoryUiModel.EntryModel(model) }
                    .insertSeparators { before, after ->
                        if (shouldSeparate(before, after))
                            HistoryUiModel.HeaderModel(after?.model?.historyEntry?.date ?: 0L)
                        else null
                    }
            }
    }

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

    fun getFriendSortedByFrequence() = historyXFriendDao.getFriendSortedByFrequence()

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

    suspend fun insertHistoryEntry(entry: HistoryEntry, friends: List<Long>) {
        database.withTransaction {
            val entryId = insertHistoryEntry(entry)
            val historyXFriends = friends.map { HistoryXFriend(entryId, it) }
            historyXFriendDao.insertHistoryXFriends(historyXFriends)
        }
    }

    suspend fun deleteAllHistoryEntries() = historyDao.deleteAll()

    private fun getAllEntries() = historyDao.getAllEntries()

    private fun getDataSource(filter: HistoryFilter): PagingSource<Int, BoundedHistoryEntry> {
        return when (filter) {
            is HistoryFilter.TypeFilter -> when (filter.chipId) {
                R.id.chipReplenishments -> getEntriesByType(1, 3)
                R.id.chipComsumptions -> getEntriesByType(0, 2)
                R.id.chipTastings -> getEntriesByType(4, 4)
                R.id.chipGiftedTo -> getEntriesByType(2, 2)
                R.id.chipGiftedBy -> getEntriesByType(3, 3)
                R.id.chipFavorites -> getFavoriteEntries()
                else -> getAllEntries()
            }

            is HistoryFilter.WineFilter -> getEntriesForWine(filter.wineId)
            is HistoryFilter.BottleFilter -> getEntriesForBottle(filter.bottleId)
            is HistoryFilter.NoFilter -> getAllEntries()
        }
    }

    private fun shouldSeparate(
        before: HistoryUiModel.EntryModel?,
        after: HistoryUiModel?
    ): Boolean {
        if (before == null && after == null) {
            return false
        }

        return if (after is HistoryUiModel.EntryModel?) {
            val beforeTimestamp =
                DateFormatter.roundToDay(before?.model?.historyEntry?.date ?: return true)
            val afterTimestamp =
                DateFormatter.roundToDay(after?.model?.historyEntry?.date ?: return false)

            beforeTimestamp != afterTimestamp
        } else false
    }

    private suspend fun ensureEntryTypeUnicityForBottle(entry: HistoryEntry) {
        when {
            entry.type.isReplenishment() ->
                historyDao.clearReplenishmentsForBottle(entry.bottleId)

            entry.type.isConsumption() ->
                historyDao.clearConsumptionsForBottle(entry.bottleId)
        }
    }
}
