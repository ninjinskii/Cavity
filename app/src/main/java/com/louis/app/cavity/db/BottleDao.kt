package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.relation.BottleAndWine
import com.louis.app.cavity.model.relation.BottleWithQGrapes
import com.louis.app.cavity.model.relation.QuantifiedBottleGrapeXRef
import com.louis.app.cavity.model.relation.QuantifiedGrapeAndGrape

@Dao
interface BottleDao {

    // ---------------Bottle---------------
    @Insert
    suspend fun insertBottle(bottle: Bottle): Long

    @Update
    suspend fun updateBottle(bottle: Bottle)

    @Delete
    suspend fun deleteBottle(bottle: Bottle)

    @Query("SELECT * FROM bottle")
    fun getAllBottles(): LiveData<List<Bottle>>

    @Query("SELECT * FROM grape")
    suspend fun getAllGrapesNotLive(): List<Grape>

    @Query("SELECT * FROM bottle WHERE bottle_id=:bottleId")
    suspend fun getBottleByIdNotLive(bottleId: Long): Bottle

    @Query("DELETE FROM bottle WHERE bottle_id=:bottleId")
    suspend fun deleteBottleById(bottleId: Long)

    @Transaction
    @Query("SELECT bottle_id, name, naming, cuvee, color, is_organic, vintage, apogee, is_favorite, count, price, currency, other_info, buy_location, buy_date, taste_comment, pdf_path, county_id FROM wine, bottle WHERE wine.wine_id = bottle.wine_id")
    suspend fun getBottlesAndWineNotLive(): List<BottleAndWine>

    @Transaction
    @Query("SELECT * FROM bottle")
    suspend fun getBottleWithQGrapesNotLive(): List<BottleWithQGrapes>

    // ---------------Grape---------------
    @Insert
    suspend fun insertGrape(grape: Grape): Long

    @Update
    suspend fun updateGrape(grape: Grape)

    @Delete
    suspend fun deleteGrape(grape: Grape)

    @Query("SELECT * FROM grape")
    fun getAllGrapes(): LiveData<List<Grape>>

    // ----------Quantified Grape---------
    @Insert
    suspend fun insertQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef)

    @Update
    suspend fun updateQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef)

    @Delete
    suspend fun deleteQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef)

    @Transaction
    @Query("SELECT * FROM q_grape WHERE bottle_id=:bottleId")
    fun getQGrapesForBottle(bottleId: Long) : LiveData<List<QuantifiedBottleGrapeXRef>>

    @Transaction
    @Query("SELECT * FROM q_grape WHERE bottle_id=:bottleId")
    suspend fun getQGrapesForBottleNotLive(bottleId: Long) : List<QuantifiedBottleGrapeXRef>

    @Transaction
    @Query("SELECT * FROM q_grape WHERE bottle_id=:bottleId AND grape_id=:grapeId")
    suspend fun getQGrape(bottleId: Long, grapeId: Long): QuantifiedBottleGrapeXRef

    @Transaction
    @Query("SELECT * FROM q_grape WHERE bottle_id=:bottleId")
    fun getQGrapesAndGrapeForBottle(bottleId: Long): LiveData<List<QuantifiedGrapeAndGrape>>

    // ---------------Review---------------
    @Insert
    suspend fun insertReview(review: Review)

    @Update
    suspend fun updateReview(review: Review)

    @Delete
    suspend fun deleteReview(review: Review)

    @Query("SELECT * FROM review")
    fun getAllReviews(): LiveData<List<Review>>

}
