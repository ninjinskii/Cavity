package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Naming
import com.louis.app.cavity.model.Wine

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

    @Transaction
    @Query("SELECT * FROM naming")
    fun getNamingsWithWines(): LiveData<List<NamingWithWines>>
}

data class NamingWithWines(
    @Embedded val naming: Naming,
    @Relation(
        parentColumn = "id",
        entityColumn = "naming_id"
    )
    val wines: List<Wine>
)
