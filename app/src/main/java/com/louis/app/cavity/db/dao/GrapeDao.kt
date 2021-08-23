package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.QGrape

@Dao
interface GrapeDao {
    @Insert
    suspend fun insertGrape(grape: Grape): Long

    @Update
    suspend fun updateGrape(grape: Grape)

    @Delete
    suspend fun deleteGrape(grape: Grape)

    @Query("SELECT * FROM grape")
    fun getAllGrapes(): LiveData<List<Grape>>

    @Query("SELECT * FROM grape")
    suspend fun getAllGrapesNotLive(): List<Grape>

    @Transaction
    @Query("SELECT * FROM grape")
    fun getGrapeWithQuantifiedGrapes(): LiveData<List<GrapeWithQGrapes>>

    @Query("DELETE FROM grape")
    fun deleteAll()
}

data class GrapeWithQGrapes(
    @Embedded val grape: Grape,
    @Relation(
        parentColumn = "id",
        entityColumn = "grape_id"
    )
    val qGrapes: List<QGrape>
)
