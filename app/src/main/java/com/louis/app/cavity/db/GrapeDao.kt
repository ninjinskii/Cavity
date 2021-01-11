package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Grape

@Dao
interface GrapeDao {
    @Insert
    suspend fun insertGrape(grape: Grape): Long

    @Update
    suspend fun updateGrape(grape: Grape)

    @Delete
    suspend fun deleteGrape(grape: Grape)

    @Query("SELECT * FROM grape")
    fun getAllGrapes(): LiveData<List<Grape>>

    @Query("SELECT * FROM grape")
    suspend fun getAllGrapesNotLive(): List<Grape>
}
