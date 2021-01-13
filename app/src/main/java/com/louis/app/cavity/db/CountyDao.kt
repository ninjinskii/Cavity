package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.relation.CountyWithWines

@Dao
interface CountyDao {
    @Insert
    fun insertCounty(county: County)

    @Update
    fun updateCounty(county: County)

    @Delete
    fun deleteCounty(county: County)

    @Query("SELECT * FROM county ORDER BY pref_order")
    fun getAllCounties(): LiveData<List<County>>

    @Query("SELECT * FROM county")
    fun getAllCountiesNotLive(): List<County>

    @Transaction
    @Query("SELECT * FROM county")
    fun getCountiesWithWines(): LiveData<List<CountyWithWines>>

    @Transaction
    @Query("SELECT * FROM county")
    fun getCountiesWithWinesNotLive(): List<CountyWithWines>
}
