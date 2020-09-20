package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.ExpertAdvice
import com.louis.app.cavity.model.Grape

@Dao
interface BottleDao {

    // ---------------Bottle---------------
    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    fun insertBottle(bottle: Bottle): Long

    @Update
    fun updateBottle(bottle: Bottle)

    @Delete
    fun deleteBottle(bottle: Bottle)

    @Query("SELECT * FROM bottle")
    fun getAllBottles(): LiveData<List<Bottle>>

    @Query("SELECT * FROM bottle WHERE id_bottle=:bottleId")
    fun getBottleByIdNotLive(bottleId: Long): Bottle

    @Query("DELETE FROM bottle WHERE id_bottle=:bottleId")
    fun deleteBottleById(bottleId: Long)

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