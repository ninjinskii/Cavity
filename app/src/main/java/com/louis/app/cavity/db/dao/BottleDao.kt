package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.*

@Dao
interface BottleDao {
    @Insert
    suspend fun insertBottle(bottle: Bottle): Long

    @Insert
    suspend fun insertBottles(bottle: List<Bottle>)

    @Update
    suspend fun updateBottle(bottle: Bottle)

    @Delete
    suspend fun deleteBottle(bottle: Bottle)

    @Query("SELECT * FROM bottle WHERE id=:bottleId")
    fun getBottleById(bottleId: Long): LiveData<Bottle>

    @Query("SELECT * FROM bottle WHERE id=:bottleId")
    suspend fun getBottleByIdNotLive(bottleId: Long): Bottle

    @Query("SELECT * FROM bottle WHERE wine_id=:wineId")
    fun getBottlesForWine(wineId: Long): LiveData<List<Bottle>>

    @Query("UPDATE bottle SET is_favorite = 1 WHERE id=:bottleId")
    suspend fun fav(bottleId: Long)

    @Query("UPDATE bottle SET is_favorite = 0 WHERE id=:bottleId")
    suspend fun unfav(bottleId: Long)

    @Query("DELETE FROM bottle WHERE id=:bottleId")
    suspend fun deleteBottleById(bottleId: Long)

    @Query("UPDATE bottle SET consumed = 1 WHERE id=:bottleId")
    suspend fun consumeBottle(bottleId: Long)

    @Query("UPDATE bottle SET consumed = 0 WHERE id=:bottleId")
    suspend fun revertBottleConsumption(bottleId: Long)

    @Query("SELECT id FROM bottle WHERE id IN (:bottleIds) AND tasting_id IS NOT NULL")
    suspend fun getTastingBottleIdsIn(bottleIds: List<Long>): List<Long>

    @Query("UPDATE bottle SET tasting_id=:tastingId WHERE bottle.id IN (:bottleIds)")
    suspend fun boundBottlesToTasting(tastingId: Long, bottleIds: List<Long>)

    @Transaction
    @Query("SELECT * FROM bottle WHERE consumed = 0")
    fun getBoundedBottles(): LiveData<List<BoundedBottle>>

    @Transaction
    @Query("SELECT bottle.* FROM wine, bottle WHERE wine.id = bottle.wine_id AND bottle.consumed = 0")
    suspend fun getBoundedBottlesNotLive(): List<BoundedBottle>

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

data class BottleWithHistoryEntries(
    @Embedded val bottle: Bottle,
    @Relation(
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
)
