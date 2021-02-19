package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagingSource
import androidx.room.*
import com.louis.app.cavity.model.HistoryEntry
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

    @Query("SELECT * FROM history_entry ORDER BY date DESC")
    fun getAllEntriesNotPagedNotLive(): List<HistoryEntry>

    @Transaction
    @Query("SELECT * FROM history_entry")
    fun getE(): LiveData<List<HistoryEntryWithFriends>>

    @Query("DELETE FROM history_entry WHERE bottle_id=:bottleId")
    suspend fun deleteEntriesForBottle(bottleId: Long)
}
