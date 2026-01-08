package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BottleDao {
    @Insert
    suspend fun insertBottle(bottle: Bottle): Long

    @Insert
    suspend fun insertBottles(bottle: List<Bottle>)

    @Update
    suspend fun updateBottle(bottle: Bottle)

    @Update
    suspend fun updateBottles(bottles: List<Bottle>)

    @Delete
    suspend fun deleteBottles(bottle: List<Bottle>)

    @Query("SELECT * FROM bottle WHERE id=:bottleId")
    fun getBottleById(bottleId: Long): LiveData<Bottle>

    @Query("SELECT * FROM bottle WHERE id=:bottleId")
    suspend fun getBottleByIdNotLive(bottleId: Long): Bottle

    @Query("SELECT * FROM bottle")
    suspend fun getAllBottlesNotLive(): List<Bottle>

    @Transaction
    @Query("SELECT * FROM bottle WHERE wine_id=:wineId ORDER BY consumed ASC, vintage")
    fun getBottlesForWine(wineId: Long): Flow<List<BottleWithHistoryEntries>>

    @Query("SELECT * FROM bottle WHERE wine_id=:wineId")
    suspend fun getBottlesForWineNotLive(wineId: Long): List<Bottle>

    @Query("UPDATE bottle SET is_favorite = 1 WHERE id=:bottleId")
    suspend fun fav(bottleId: Long)

    @Query("UPDATE bottle SET is_favorite = 0 WHERE id=:bottleId")
    suspend fun unfav(bottleId: Long)

    @Query("UPDATE bottle SET tasting_id = NULL WHERE id=:bottleId")
    suspend fun removeTastingForBottle(bottleId: Long)

    @Query("DELETE FROM bottle WHERE id=:bottleId")
    suspend fun deleteBottleById(bottleId: Long)

    @Query("UPDATE bottle SET consumed = 1 WHERE id=:bottleId")
    suspend fun consumeBottle(bottleId: Long)

    @Query("UPDATE bottle SET consumed = 0, tasting_id = NULL WHERE id=:bottleId")
    suspend fun revertBottleConsumption(bottleId: Long)

    @Query("SELECT id FROM bottle WHERE id IN (:bottles) AND tasting_id IS NOT NULL")
    suspend fun getTastingBottleIdsIn(bottles: List<Long>): List<Long>

    @Query("UPDATE bottle SET tasting_id=:tastingId WHERE bottle.id IN (:bottles)")
    suspend fun boundBottlesToTasting(tastingId: Long, bottles: List<Long>)

    @Query("SELECT DISTINCT buy_location FROM bottle WHERE length(buy_location) > 0")
    fun getAllBuyLocations(): LiveData<List<String>>

    @Query("SELECT DISTINCT storage_location FROM bottle WHERE length(storage_location) > 0")
    fun getAllStorageLocations(): LiveData<List<String>>

    @Transaction
    @Query("SELECT * FROM bottle")
    fun getBoundedBottles(): LiveData<List<BoundedBottle>>

    @Transaction
    @Query("SELECT * FROM bottle WHERE id=:bottleId")
    suspend fun getBoundedBottleByIdNotLive(bottleId: Long): BoundedBottle

    @Query("DELETE FROM bottle")
    suspend fun deleteAll()
}

data class BottleAndWine(
    @Embedded val bottle: Bottle,
    @Relation(
        entity = Wine::class,
        parentColumn = "wine_id",
        entityColumn = "id"
    )
    val wine: Wine,
)

data class BottleWithTastingActions(
    @Embedded val bottle: Bottle,
    @Relation(
        entity = Wine::class,
        parentColumn = "wine_id",
        entityColumn = "id"
    )
    val wine: Wine,
    @Relation(
        entity = TastingAction::class,
        parentColumn = "id",
        entityColumn = "bottle_id"
    )
    val tastingActions: List<TastingAction>,
)

data class BottleWithHistoryEntries(
    @Embedded val bottle: Bottle,
    @Relation(
        entity = HistoryEntry::class,
        parentColumn = "id",
        entityColumn = "bottle_id"
    )
    val historyEntries: List<HistoryEntry>
)

data class BoundedBottle(
    @Embedded val bottle: Bottle,
    @Relation(
        entity = Wine::class,
        parentColumn = "wine_id",
        entityColumn = "id"
    )
    val wine: Wine,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = QGrape::class,
            parentColumn = "bottle_id",
            entityColumn = "grape_id"
        )
    )
    val grapes: List<Grape>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = FReview::class,
            parentColumn = "bottle_id",
            entityColumn = "review_id"
        )
    )
    val reviews: List<Review>,
    @Relation(
        entity = HistoryEntry::class,
        parentColumn = "id",
        entityColumn = "bottle_id",
    )
    val historyEntriesWithFriends: List<HistoryEntryWithFriends>,
)
