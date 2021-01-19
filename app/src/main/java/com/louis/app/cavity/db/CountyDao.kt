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

    @Transaction
    @Query("SELECT * FROM county ORDER BY pref_order")
    suspend fun getCountiesWithWinesNotLive(): List<CountyWithWines>

    @Transaction
    suspend fun swapCounties(county1Id: Long, pos1: Int, county2Id: Long, pos2: Int) {
        L.v("county1Id: $county1Id, pos: $pos1")
        L.v("county2Id: $county2Id, pos: $pos2")
        setCountyOrder(county1Id, pos1)
        setCountyOrder(county2Id, pos2)
    }

    @Query("UPDATE county SET pref_order=:newOrder WHERE county_id=:countyId")
    suspend fun setCountyOrder(countyId: Long, newOrder: Int)

    // TODO: remove
    @Query("UPDATE county SET pref_order = county_id - 1")
    suspend fun resetOrder()
}
