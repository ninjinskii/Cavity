package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.relation.WineWithBottles

@Dao
interface WineDao {

    @Insert
    fun insertWine(wine: Wine)

    @Update
    fun updateWine(wine: Wine)

    @Delete
    fun deleteWine(wine: Wine)

    @Query("DELETE FROM wine WHERE wine_id =:wineId")
    fun deleteWineById(wineId: Long)

    @Query("SELECT * FROM wine")
    fun getAllWines(): LiveData<List<Wine>>

    @Query("SELECT * FROM wine WHERE wine_id =:wineId")
    fun getWineById(wineId: Long): LiveData<Wine>

    @Query("SELECT * FROM wine WHERE wine_id =:wineId")
    fun getWineByIdNotLive(wineId: Long): Wine

    @Transaction
    @Query("SELECT * FROM wine ORDER BY color, naming")
    fun getWineWithBottles(): LiveData<List<WineWithBottles>>

    @Transaction
    @Query("SELECT * FROM wine ORDER BY color, naming")
    fun getWineWithBottlesNotLive(): List<WineWithBottles>

    @Transaction
    @Query("SELECT * FROM wine WHERE county_id =:countyId ORDER BY color, naming")
    fun getWineWithBottlesByCounty(countyId: Long): LiveData<List<WineWithBottles>>
}
