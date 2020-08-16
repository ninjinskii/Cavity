package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Wine

@Dao
interface WineDao {

    @Insert
    fun insertWine(wine: Wine)

    @Update
    fun updateWine(wine: Wine)

    @Delete
    fun deleteWine(wine: Wine)

    @Query("SELECT * FROM wine")
    fun getAllWines(): LiveData<List<Wine>>
}