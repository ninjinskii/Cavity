package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.relation.CountyWithWines
import com.louis.app.cavity.util.L

@Dao
interface CountyDao {
    @Insert
    fun insertCounty(county: County)

    @Update
    suspend fun updateCounty(county: County)

    @Update
    suspend fun updateCounties(counties: List<County>)

    @Delete
    fun deleteCounty(county: County)

    @Query("DELETE FROM county WHERE county_id=:countyId")
    suspend fun deleteCounty(countyId: Long)

    @Query("SELECT * FROM county ORDER BY pref_order")
    fun getAllCounties(): LiveData<List<County>>

    @Query("SELECT * FROM county ORDER BY pref_order")
    suspend fun getAllCountiesNotLive(): List<County>

    @Transaction
    @Query("SELECT * FROM county ORDER BY pref_order")
    fun getCountiesWithWines(): LiveData<List<CountyWithWines>>
}
