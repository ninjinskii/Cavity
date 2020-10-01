package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.ExpertAdvice
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.relation.BottleWithExpertAdvices
import com.louis.app.cavity.model.relation.BottleWithGrapes
import com.louis.app.cavity.model.relation.BottleAndWine

@Dao
interface BottleDao {

    // ---------------Bottle---------------
    @Insert
    fun insertBottle(bottle: Bottle): Long

    @Update
    fun updateBottle(bottle: Bottle)

    @Delete
    fun deleteBottle(bottle: Bottle)

    @Query("SELECT * FROM bottle")
    fun getAllBottles(): LiveData<List<Bottle>>

    @Transaction
    @Query("SELECT * FROM bottle WHERE bottle_id=:bottleId")
    fun getBottleWithGrapesById(bottleId: Long): BottleWithGrapes

    @Transaction
    @Query("SELECT * FROM bottle WHERE bottle_id=:bottleId")
    fun getBottleWithExpertAdvicesById(bottleId: Long): BottleWithExpertAdvices

    @Query("SELECT * FROM bottle WHERE bottle_id=:bottleId")
    fun getBottleByIdNotLive(bottleId: Long): Bottle

    @Query("DELETE FROM bottle WHERE bottle_id=:bottleId")
    fun deleteBottleById(bottleId: Long)

    @Query("SELECT bottle_id, name, naming, cuvee, color, is_organic, vintage, apogee, is_favorite, count, price, currency, other_info, buy_location, buy_date, taste_comment, pdf_path, county_id FROM wine, bottle WHERE wine.wine_id = bottle.wine_id")
    fun getBottlesAndWineNotLive(): List<BottleAndWine>

    @Transaction
    @Query("SELECT * FROM bottle")
    fun getBottleWithGrapesNotLive() : List<BottleWithGrapes>

    // ---------------Grape---------------
    @Insert
    fun insertGrape(grape: Grape)

    @Update
    fun updateGrape(grape: Grape)

    @Delete
    fun deleteGrape(grape: Grape)

    @Query("SELECT * FROM grape")
    fun getAllGrapes(): LiveData<List<Grape>>

    // ---------------ExpertAdvice---------------
    @Insert
    fun insertAdvice(advice: ExpertAdvice)

    @Update
    fun updateAdvice(advice: ExpertAdvice)

    @Delete
    fun deleteAdvice(advice: ExpertAdvice)

    @Query("SELECT * FROM expert_advice")
    fun getAllExpertAdvices(): LiveData<List<ExpertAdvice>>

}
