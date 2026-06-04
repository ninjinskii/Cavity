package com.louis.app.cavity.db.dao

import androidx.room.*
import com.louis.app.cavity.domain.stats.QGrapeAndGrape
import com.louis.app.cavity.domain.stats.Stat
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.QGrape
import com.louis.app.cavity.util.ColorUtil
import kotlinx.coroutines.flow.Flow

@Dao
interface QuantifiedGrapeDao {
    @Insert
    suspend fun insertQGrape(qGrape: QGrape)

    @Insert
    suspend fun insertQGrapes(qGrapes: List<QGrape>)

    @Update
    suspend fun updateQGrape(qGrape: QGrape)

    @Delete
    suspend fun deleteQGrape(qGrape: QGrape)

    @Query("SELECT * FROM q_grape")
    suspend fun getAllQGrapesNotLive(): List<QGrape>

    @Transaction
    @Query("SELECT * FROM q_grape WHERE bottle_id=:bottleId AND grape_id=:grapeId")
    suspend fun getQGrape(bottleId: Long, grapeId: Long): QGrape

    @Transaction
    @Query("SELECT * FROM q_grape WHERE bottle_id=:bottleId ORDER BY percentage DESC")
    fun getQGrapesAndGrapeForBottle(bottleId: Long): Flow<List<QGrapeAndGrape>>

    @Transaction
    @Query("SELECT * FROM q_grape WHERE bottle_id=:bottleId")
    suspend fun getQGrapesAndGrapeForBottleNotLive(bottleId: Long): List<QGrapeAndGrape>

    @Query("DELETE FROM q_grape WHERE bottle_id=:bottleId")
    suspend fun clearAllQGrapesForBottle(bottleId: Long)

    @Query("DELETE FROM q_grape")
    suspend fun deleteAll()
}


