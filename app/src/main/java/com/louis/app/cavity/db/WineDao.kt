package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.County
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

    @Insert
    fun insertCounty(county: County)

    @Update
    fun updateCounty(county: County)

    @Delete
    fun deleteCounty(county: County)

    @Query("SELECT * FROM wine")
    fun getAllWines(): LiveData<List<Wine>>

    @Transaction
    @Query("SELECT * FROM wine")
    fun getWineWithBottles(): LiveData<List<WineWithBottles>>

    @Transaction
    @Query("SELECT * FROM wine WHERE id_county =:countyId")
    fun getWineWithBottlesByCounty(countyId: Long): LiveData<List<WineWithBottles>>

    @Query("SELECT * FROM county ORDER BY pref_order")
    fun getAllCounties(): LiveData<List<County>>

    @Query("SELECT * FROM county")
    fun getAllCountiesNotLive(): List<County>
}