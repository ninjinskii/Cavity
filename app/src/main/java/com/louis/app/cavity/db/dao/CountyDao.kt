package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Wine

@Dao
interface CountyDao {
    @Insert
    suspend fun insertCounties(county: County)

    @Insert
    suspend fun insertCounties(counties: List<County>)

    @Update
    suspend fun updateCounty(county: County)

    @Update
    suspend fun updateCounties(counties: List<County>)

    @Delete
    fun deleteCounty(county: County)

    @Query("DELETE FROM county WHERE id=:countyId")
    suspend fun deleteCounty(countyId: Long)

    @Query("SELECT * FROM county ORDER BY pref_order")
    fun getAllCounties(): LiveData<List<County>>

    @Query("SELECT * FROM county ORDER BY pref_order")
    suspend fun getAllCountiesNotLive(): List<County>

    @Transaction
    @Query("SELECT * FROM county ORDER BY pref_order")
    fun getCountiesWithWines(): LiveData<List<CountyWithWines>>

    @Query("DELETE FROM county")
    suspend fun deleteAll()
}

data class CountyWithWines(
    @Embedded val county: County,
    @Relation(
        parentColumn = "id",
        entityColumn = "county_id"
    )
    val wines: List<Wine>
)
