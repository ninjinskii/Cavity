package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.louis.app.cavity.model.*

@Dao
interface HistoryDao {
    @Insert
    suspend fun insertEntry(entry: HistoryEntry): Long

    @Insert
    suspend fun insertEntries(entry: List<HistoryEntry>)

    @Update
    suspend fun updateEntry(entry: HistoryEntry)

    @Delete
    fun deleteEntry(entry: HistoryEntry)

    @Query("DELETE FROM history_entry WHERE bottle_id=:bottleId")
    suspend fun deleteEntriesForBottle(bottleId: Long)

    @Query("DELETE FROM history_entry WHERE bottle_id=:bottleId AND (type = 0 OR type = 2 OR type = 4)")
    suspend fun clearConsumptionsForBottle(bottleId: Long)

    @Query("DELETE FROM history_entry WHERE bottle_id=:bottleId AND (type = 1 OR type = 3)")
    suspend fun clearReplenishmentsForBottle(bottleId: Long)

    @Query("SELECT * FROM history_entry ORDER BY date DESC")
    suspend fun getAllEntriesNotPagedNotLive(): List<HistoryEntry>

    // Divide by 1000 to convert Java milliseconds timestamps to unix timestamp (seconds)
    @Query(
        """SELECT DISTINCT strftime('%Y', date / 1000, 'unixepoch') as year,
                strftime('%s', date / 1000, 'unixepoch', 'start of year') * 1000 as yearStart, 
                strftime('%s', date / 1000, 'unixepoch', '+1 year', 'start of year') * 1000 as yearEnd
                FROM history_entry 
                ORDER BY date ASC"""
    )
    fun getYears(): LiveData<List<Year>>

    @Transaction
    @Query("SELECT * FROM history_entry ORDER BY date DESC")
    fun getAllEntries(): PagingSource<Int, BoundedHistoryEntry>

    @Transaction
    @Query("SELECT * FROM history_entry WHERE type=:type1 OR type=:type2 ORDER BY date DESC")
    fun getEntriesByType(type1: Int, type2: Int): PagingSource<Int, BoundedHistoryEntry>

    @Transaction
    @Query("SELECT * FROM history_entry WHERE favorite = 1 ORDER BY date DESC")
    fun getFavoriteEntries(): PagingSource<Int, BoundedHistoryEntry>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM history_entry INNER JOIN bottle ON bottle.id = bottle_id WHERE wine_id=:wineId ORDER BY date DESC")
    fun getEntriesForWine(wineId: Long): PagingSource<Int, BoundedHistoryEntry>

    @Transaction
    @Query("SELECT * FROM history_entry WHERE bottle_id=:bottleId ORDER BY date DESC")
    fun getEntriesForBottle(bottleId: Long): PagingSource<Int, BoundedHistoryEntry>

    @Transaction
    @Query("SELECT * FROM history_entry WHERE bottle_id=:bottleId AND (type = 1 OR type = 3) LIMIT 1")
    fun getReplenishmentForBottleNotPaged(bottleId: Long): LiveData<HistoryEntryWithFriends?>

    @Transaction
    @Query("SELECT * FROM history_entry WHERE date < :date ORDER BY date DESC")
    fun getEntriesForDate(date: Long): PagingSource<Int, BoundedHistoryEntry>

    @Transaction
    @Query("SELECT * FROM history_entry ORDER BY date DESC")
    fun getBoundedEntriesNotPagedNotLive(): List<BoundedHistoryEntry>

    @Transaction
    @Query("SELECT * FROM history_entry WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getBoundedEntriesBetween(start: Long, end: Long): LiveData<List<BoundedHistoryEntry>>

    @Query("DELETE FROM history_entry")
    suspend fun deleteAll()
}

data class Year(val year: String, val yearStart: Long, val yearEnd: Long) {
    override fun toString() = year
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
    var tastingWithBottles: TastingWithBottles?,
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
