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
    fun insertEntry(entry: HistoryEntry)

    @Update
    fun updateEntry(entry: HistoryEntry)

    @Delete
    fun deleteEntry(entry: HistoryEntry)

    @Transaction
    @Query("SELECT * FROM history_entry ORDER BY date")
    fun getAllEntries(): PagingSource<Int, HistoryEntryWithBottleAndTastingAndFriends>

    @Transaction
    @Query("SELECT * FROM history_entry")
    fun getE(): LiveData<List<HistoryEntryWithFriends>>
}
