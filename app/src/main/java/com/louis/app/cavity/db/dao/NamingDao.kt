package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Naming

@Dao
interface NamingDao {
    @Insert
    suspend fun insertNaming(naming: Naming)

    @Update
    suspend fun updateNaming(naming: Naming)

    @Delete
    suspend fun deleteNaming(naming: Naming)

    @Query("SELECT * FROM naming WHERE county_id=:countyId")
    fun getNamingsForCounty(countyId: Long): LiveData<List<Naming>>

    @Query("SELECT * FROM naming WHERE id=:namingId")
    suspend fun getNamingById(namingId: Long): Naming
}
