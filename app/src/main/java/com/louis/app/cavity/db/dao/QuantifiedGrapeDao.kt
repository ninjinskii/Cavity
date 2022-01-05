package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.QGrape

@Dao
interface QuantifiedGrapeDao {
    @Insert
    suspend fun insertQGrape(qGrape: QGrape)

    @Insert
    suspend fun insertQGrape(qGrapes: List<QGrape>)

    @Update
    suspend fun updateQGrape(qGrape: QGrape)

    @Delete
    suspend fun deleteQGrape(qGrape: QGrape)

    @Transaction
    @Query("SELECT * FROM q_grape WHERE bottle_id=:bottleId")
    suspend fun getQGrapesForBottleNotLive(bottleId: Long): List<QGrape>

    @Transaction
    @Query("SELECT * FROM q_grape WHERE bottle_id=:bottleId AND grape_id=:grapeId")
    suspend fun getQGrape(bottleId: Long, grapeId: Long): QGrape

    @Transaction
    @Query("SELECT * FROM q_grape WHERE bottle_id=:bottleId")
    fun getQGrapesAndGrapeForBottle(bottleId: Long): LiveData<List<QGrapeAndGrape>>

    @Transaction
    @Query("SELECT * FROM q_grape WHERE bottle_id=:bottleId")
    suspend fun getQGrapesAndGrapeForBottleNotLive(bottleId: Long): List<QGrapeAndGrape>

    @Query("DELETE FROM q_grape WHERE bottle_id=:bottleId")
    suspend fun clearAllQGrapesForBottle(bottleId: Long)
}

data class QGrapeAndGrape(
    @Embedded val qGrape: QGrape,
    @Relation(
        entity = Grape::class,
        parentColumn = "grape_id",
        entityColumn = "id",
        projection = ["name"]
    )
    val grapeName: String
) :
    NewStat {
    @Ignore
    override val percentage = qGrape.percentage.toFloat()
    @Ignore
    override val label = grapeName
}


