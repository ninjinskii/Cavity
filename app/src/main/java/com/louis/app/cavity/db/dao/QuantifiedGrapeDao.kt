package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.QGrape
import com.louis.app.cavity.util.ColorUtil

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
    fun getQGrapesAndGrapeForBottle(bottleId: Long): LiveData<List<QGrapeAndGrape>>

    @Transaction
    @Query("SELECT * FROM q_grape WHERE bottle_id=:bottleId")
    suspend fun getQGrapesAndGrapeForBottleNotLive(bottleId: Long): List<QGrapeAndGrape>

    @Query("DELETE FROM q_grape WHERE bottle_id=:bottleId")
    suspend fun clearAllQGrapesForBottle(bottleId: Long)

    @Query("DELETE FROM q_grape")
    suspend fun deleteAll()
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
    Stat {
    @Ignore
    override val percentage = qGrape.percentage.toFloat()

    @Ignore
    override val count = -1

    @Ignore
    override val label = grapeName

    @Ignore
    override val color = ColorUtil.next()
}


