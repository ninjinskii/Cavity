package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.relation.BottleAndWine
import com.louis.app.cavity.model.relation.BottleAndWineWithQGrapesAndFReviews
import com.louis.app.cavity.model.relation.BottleWithQGrapes

@Dao
interface BottleDao {
    @Insert
    suspend fun insertBottle(bottle: Bottle): Long

    @Update
    suspend fun updateBottle(bottle: Bottle)

    @Delete
    suspend fun deleteBottle(bottle: Bottle)

    @Query("SELECT * FROM bottle")
    fun getAllBottles(): LiveData<List<Bottle>>

    @Query("SELECT * FROM bottle WHERE bottle_id=:bottleId")
    fun getBottleById(bottleId: Long): LiveData<Bottle>

    @Query("SELECT * FROM bottle WHERE bottle_id=:bottleId")
    suspend fun getBottleByIdNotLive(bottleId: Long): Bottle

    @Query("DELETE FROM bottle WHERE bottle_id=:bottleId")
    suspend fun deleteBottleById(bottleId: Long)

    @Query("UPDATE bottle SET is_favorite = 1 WHERE bottle_id=:bottleId")
    suspend fun fav(bottleId: Long)

    @Query("UPDATE bottle SET is_favorite = 0 WHERE bottle_id=:bottleId")
    suspend fun unfav(bottleId: Long)

    @Query("UPDATE bottle SET count=:count + bottle.count WHERE bottle_id=:bottleId")
    suspend fun addBottles(bottleId: Long, count: Int)

    @Query("UPDATE bottle SET count=:count - bottle.count WHERE bottle_id=:bottleId")
    suspend fun removeBottles(bottleId: Long, count: Int)

    @Transaction
    @Query("SELECT wine.wine_id, bottle_id, name, naming, cuvee, color, is_organic, vintage, apogee, is_favorite, count, price, currency, other_info, buy_location, buy_date, taste_comment, pdf_path, county_id FROM wine, bottle WHERE wine.wine_id = bottle.wine_id")
    suspend fun getBottlesAndWineNotLive(): List<BottleAndWine>

    @Transaction
    @Query("SELECT * FROM bottle")
    suspend fun getBottleWithQGrapesNotLive(): List<BottleWithQGrapes>

    @Transaction
    @Query("SELECT * FROM wine, bottle WHERE wine.wine_id = bottle.wine_id")
    suspend fun getBottleAndWineWithQGrapesAndFReview(): List<BottleAndWineWithQGrapesAndFReviews>

}
