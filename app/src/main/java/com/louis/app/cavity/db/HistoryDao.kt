package com.louis.app.cavity.db

import androidx.paging.DataSource
import androidx.paging.PagingSource
import androidx.room.*
import com.louis.app.cavity.model.HistoryEntry

@Dao
interface HistoryDao {
    @Insert
    fun insertEntry(entry: HistoryEntry)

    @Update
    fun updateEntry(entry: HistoryEntry)

    @Delete
    fun deleteEntry(entry: HistoryEntry)

    @Query("SELECT * FROM history_entry ORDER BY date")
    fun getAllEntries(): PagingSource<Int, HistoryEntry>
}
