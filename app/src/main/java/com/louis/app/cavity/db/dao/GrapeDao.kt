package com.louis.app.cavity.db.dao

import androidx.room.*
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.QGrape
import kotlinx.coroutines.flow.Flow

@Dao
interface GrapeDao {
    @Insert
    suspend fun insertGrape(grape: Grape): Long

    @Insert
    suspend fun insertGrapes(grapes: List<Grape>)

    @Update
    suspend fun updateGrape(grape: Grape)

    @Delete
    suspend fun deleteGrape(grape: Grape)

    @Query("SELECT * FROM grape ORDER BY name")
    fun getAllGrapes(): Flow<List<Grape>>

    @Query("SELECT * FROM grape ORDER BY name")
    suspend fun getAllGrapesNotLive(): List<Grape>

    @Transaction
    @Query("SELECT * FROM grape")
    fun getGrapeWithQuantifiedGrapes(): Flow<List<GrapeWithQGrapes>>

    @Query("DELETE FROM grape")
    suspend fun deleteAll()
}

data class GrapeWithQGrapes(
    @Embedded val grape: Grape,
    @Relation(
        parentColumn = "id",
        entityColumn = "grape_id"
    )
    val qGrapes: List<QGrape>
)
