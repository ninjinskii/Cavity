package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.relation.crossref.QuantifiedBottleGrapeXRef
import com.louis.app.cavity.model.relation.grape.QuantifiedGrapeAndGrape

@Dao
interface QuantifiedGrapeDao {
    @Insert
    suspend fun insertQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef)

    @Insert
    suspend fun insertQuantifiedGrapes(qGrapes: List<QuantifiedBottleGrapeXRef>)

    @Update
    suspend fun updateQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef)

    @Delete
    suspend fun deleteQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef)

    @Transaction
    @Query("SELECT * FROM q_grape WHERE bottle_id=:bottleId")
    suspend fun getQGrapesForBottleNotLive(bottleId: Long): List<QuantifiedBottleGrapeXRef>

    @Transaction
    @Query("SELECT * FROM q_grape WHERE bottle_id=:bottleId AND grape_id=:grapeId")
    suspend fun getQGrape(bottleId: Long, grapeId: Long): QuantifiedBottleGrapeXRef

    @Transaction
    @Query("SELECT * FROM q_grape WHERE bottle_id=:bottleId")
    fun getQGrapesAndGrapeForBottle(bottleId: Long): LiveData<List<QuantifiedGrapeAndGrape>>

    @Transaction
    @Query("SELECT * FROM q_grape WHERE bottle_id=:bottleId")
    suspend fun getQGrapesAndGrapeForBottleNotLive(bottleId: Long): List<QuantifiedGrapeAndGrape>

    @Query("DELETE FROM q_grape WHERE bottle_id=:bottleId")
    suspend fun clearAllQGrapesForBottle(bottleId: Long)
}
