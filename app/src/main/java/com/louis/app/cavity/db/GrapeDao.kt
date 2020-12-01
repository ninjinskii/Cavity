package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.louis.app.cavity.model.Grape

interface GrapeDao {
    @Insert
    suspend fun insertGrape(grape: Grape): Long

    @Update
    suspend fun updateGrape(grape: Grape)

    @Delete
    suspend fun deleteGrape(grape: Grape)

    @Query("SELECT * FROM grape")
    fun getAllGrapes(): LiveData<List<Grape>>
}
