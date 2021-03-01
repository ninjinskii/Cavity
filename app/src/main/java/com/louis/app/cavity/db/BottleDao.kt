package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.relation.bottle.BottleAndWineWithQGrapesAndFReviews

@Dao
interface BottleDao {
    @Insert
    suspend fun insertBottle(bottle: Bottle): Long

    @Update
    suspend fun updateBottle(bottle: Bottle)

    @Delete
    suspend fun deleteBottle(bottle: Bottle)

    @Query("SELECT * FROM bottle WHERE id=:bottleId")
    fun getBottleById(bottleId: Long): LiveData<Bottle>

    @Query("SELECT * FROM bottle WHERE id=:bottleId")
    suspend fun getBottleByIdNotLive(bottleId: Long): Bottle

    @Query("UPDATE bottle SET is_favorite = 1 WHERE id=:bottleId")
    suspend fun fav(bottleId: Long)

    @Query("UPDATE bottle SET is_favorite = 0 WHERE id=:bottleId")
    suspend fun unfav(bottleId: Long)

    @Query("UPDATE bottle SET count=:count + bottle.count WHERE id=:bottleId")
    suspend fun addBottles(bottleId: Long, count: Int)

    @Query("DELETE FROM bottle WHERE id=:bottleId")
    suspend fun deleteBottleById(bottleId: Long)

    @Query("UPDATE bottle SET consumed = 1 WHERE id=:bottleId")
    suspend fun consumeBottle(bottleId: Long)

    @Query("UPDATE bottle SET consumed = 0 WHERE id=:bottleId")
    suspend fun revertBottleConsumption(bottleId: Long)

    @Transaction
    @Query("SELECT bottle.* FROM wine, bottle WHERE wine.id = bottle.wine_id AND bottle.consumed = 0")
    suspend fun getBottleAndWineWithQGrapesAndFReview(): List<BottleAndWineWithQGrapesAndFReviews>
}
