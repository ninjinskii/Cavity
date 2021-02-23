package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagingSource
import androidx.room.*
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.HistoryEntryType
import com.louis.app.cavity.model.relation.history.HistoryEntryWithBottleAndTastingAndFriends
import com.louis.app.cavity.model.relation.history.HistoryEntryWithFriends

@Dao
interface HistoryDao {
    @Insert
    fun insertEntry(entry: HistoryEntry): Long

    @Update
    fun updateEntry(entry: HistoryEntry)

    @Delete
    fun deleteEntry(entry: HistoryEntry)

    @Transaction
    @Query("SELECT * FROM history_entry ORDER BY date DESC")
    fun getAllEntries(): PagingSource<Int, HistoryEntryWithBottleAndTastingAndFriends>

    @Transaction
    @Query("SELECT * FROM history_entry WHERE type=:type1 OR type=:type2 ORDER BY date DESC")
    fun getEntriesByType(type1: Int, type2: Int): PagingSource<Int, HistoryEntryWithBottleAndTastingAndFriends>

    @Transaction
    @Query("SELECT * FROM history_entry WHERE favorite = 1 ORDER BY date DESC")
    fun getFavoriteEntries(): PagingSource<Int, HistoryEntryWithBottleAndTastingAndFriends>

    @Transaction
    @Query("SELECT * FROM history_entry WHERE bottle_id=:bottleId ORDER BY date DESC")
    fun getEntriesForBottle(bottleId: Long): PagingSource<Int, HistoryEntryWithBottleAndTastingAndFriends>

    @Query("SELECT * FROM history_entry ORDER BY date DESC")
    fun getAllEntriesNotPagedNotLive(): List<HistoryEntry>

    @Transaction
    @Query("SELECT * FROM history_entry")
    fun getE(): LiveData<List<HistoryEntryWithFriends>>

    @Query("DELETE FROM history_entry WHERE bottle_id=:bottleId")
    suspend fun deleteEntriesForBottle(bottleId: Long)
}
