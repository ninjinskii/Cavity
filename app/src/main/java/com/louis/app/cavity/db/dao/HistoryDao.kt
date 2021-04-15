package com.louis.app.cavity.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.louis.app.cavity.model.*

@Dao
interface HistoryDao {
    @Insert
    fun insertEntry(entry: HistoryEntry): Long

    @Update
    suspend fun updateEntry(entry: HistoryEntry)

    @Delete
    fun deleteEntry(entry: HistoryEntry)

    @Transaction
    @Query("SELECT * FROM history_entry ORDER BY date DESC")
    fun getAllEntries(): PagingSource<Int, BoundedHistoryEntry>

    @Transaction
    @Query("SELECT * FROM history_entry WHERE type=:type1 OR type=:type2 ORDER BY date DESC")
    fun getEntriesByType(type1: Int, type2: Int): PagingSource<Int, BoundedHistoryEntry>

    @Transaction
    @Query("SELECT * FROM history_entry WHERE favorite = 1 ORDER BY date DESC")
    fun getFavoriteEntries(): PagingSource<Int, BoundedHistoryEntry>

    @Transaction
    @Query("SELECT * FROM history_entry WHERE bottle_id=:bottleId ORDER BY date DESC")
    fun getEntriesForBottle(bottleId: Long): PagingSource<Int, BoundedHistoryEntry>

    @Query("SELECT * FROM history_entry ORDER BY date DESC")
    fun getAllEntriesNotPagedNotLive(): List<HistoryEntry>

    @Query("DELETE FROM history_entry WHERE bottle_id=:bottleId")
    suspend fun deleteEntriesForBottle(bottleId: Long)

    @Query("DELETE FROM history_entry WHERE bottle_id=:bottleId AND type = 0 OR type = 2 OR type = 4")
    suspend fun onBottleConsumptionReverted(bottleId: Long)

    @Query("DELETE FROM history_entry WHERE bottle_id=:bottleId AND type = 1 OR type = 3")
    suspend fun clearExistingReplenishments(bottleId: Long)
}

data class BoundedHistoryEntry(
    @Embedded
    val historyEntry: HistoryEntry,
    @Relation(
        entity = Bottle::class,
        parentColumn = "bottle_id",
        entityColumn = "id"
    )
    val bottleAndWine: BottleAndWine,
    @Relation(
        entity = Tasting::class,
        parentColumn = "tasting_id",
        entityColumn = "id"
    )
    var tasting: TastingWithBottles?,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = HistoryXFriend::class,
            parentColumn = "history_entry_id",
            entityColumn = "friend_id"
        )
    )
    val friends: List<Friend>
)

data class HistoryEntryWithFriends(
    @Embedded val historyEntry: HistoryEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = HistoryXFriend::class,
            parentColumn = "history_entry_id",
            entityColumn = "friend_id"
        )
    )
    val friends: List<Friend>
)
