package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Naming
import com.louis.app.cavity.model.relation.NamingWithWinesAndBottles

@Dao
interface NamingDao {
    @Insert
    suspend fun insertNaming(naming: Naming)

    @Update
    suspend fun updateNaming(naming: Naming)

    @Delete
    suspend fun deleteNaming(naming: Naming)

    @Transaction
    @Query("SELECT * FROM naming WHERE county_id =:countyId ORDER BY naming")
    fun getBottlesForCounty(countyId: Long): LiveData<List<NamingWithWinesAndBottles>>
}
