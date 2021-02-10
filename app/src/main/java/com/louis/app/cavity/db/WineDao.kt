package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.relation.wine.WineWithBottles

@Dao
interface WineDao {
    @Insert
    fun insertWine(wine: Wine)

    @Update
    fun updateWine(wine: Wine)

    @Delete
    fun deleteWine(wine: Wine)

    @Query("DELETE FROM wine WHERE id =:wineId")
    fun deleteWineById(wineId: Long)

    @Query("SELECT * FROM wine WHERE id =:wineId")
    fun getWineById(wineId: Long): LiveData<Wine>

    @Query("SELECT * FROM wine WHERE id =:wineId")
    fun getWineByIdNotLive(wineId: Long): Wine

    @Transaction
    @Query("SELECT * FROM wine WHERE county_id =:countyId ORDER BY color, naming")
    fun getWineWithBottlesByCounty(countyId: Long): LiveData<List<WineWithBottles>>

//    @Transaction
//    @Query("SELECT * FROM wine INNER JOIN bottle ON wine.id = bottle.wine_id WHERE county_id =:countyId AND consumed = 0 ORDER BY color, naming")
//    fun getWineWithBottlesByCounty(countyId: Long): LiveData<List<WineWithBottles>>
}
