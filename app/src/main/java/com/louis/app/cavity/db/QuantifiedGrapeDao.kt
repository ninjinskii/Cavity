package com.louis.app.cavity.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.relation.QuantifiedBottleGrapeXRef
import com.louis.app.cavity.model.relation.QuantifiedGrapeAndGrape
@Dao
interface QuantifiedGrapeDao {
    @Insert
    suspend fun insertQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef)

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
}
