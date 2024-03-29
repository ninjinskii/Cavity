package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.model.TastingXFriend

@Dao
interface TastingDao {
    @Insert
    suspend fun insertTasting(tasting: Tasting): Long

    @Insert
    suspend fun insertTastings(tasting: List<Tasting>)

    @Update
    suspend fun updateTasting(tasting: Tasting)

    @Delete
    suspend fun deleteTasting(tasting: Tasting)

    @Delete
    suspend fun deleteTastings(tastings: List<Tasting>)

    @Query("SELECT * FROM tasting WHERE date < julianday(\"now\") ORDER BY date DESC LIMIT 1")
    fun getLastTasting(): LiveData<Tasting>

    @Query("SELECT * FROM tasting")
    suspend fun getAllTastingsNotLive(): List<Tasting>

    @Query("SELECT * FROM tasting WHERE id=:tastingId")
    suspend fun getTastingById(tastingId: Long): Tasting?

    @Query("SELECT * FROM tasting WHERE (SELECT COUNT(*) FROM bottle WHERE tasting_id = tasting.id) = 0")
    suspend fun getEmptyTastings(): List<Tasting>

    @Transaction
    @Query("SELECT * FROM tasting WHERE id=:tastingId")
    suspend fun getTastingWithFriendsById(tastingId: Long): BoundedTasting?

    @Transaction
    @Query("SELECT * FROM tasting WHERE done = 0")
    fun getUndoneTastings(): LiveData<List<BoundedTasting>>

    @Transaction
    @Query("SELECT * FROM bottle WHERE bottle.tasting_id=:tastingId AND consumed = 0")
    fun getBottlesWithTastingActionsForTasting(tastingId: Long): LiveData<List<BottleWithTastingActions>>

    @Transaction
    @Query("SELECT * FROM bottle WHERE bottle.tasting_id=:tastingId")
    suspend fun getBottlesWithTastingActionsForTastingNotLive(tastingId: Long): List<BottleWithTastingActions>

    @Query("DELETE FROM tasting")
    suspend fun deleteAll()
}

data class TastingWithBottles(
    @Embedded var tasting: Tasting,
    @Relation(
        parentColumn = "id",
        entityColumn = "tasting_id"
    )
    var bottles: List<Bottle>
)

data class BoundedTasting(
    @Embedded val tasting: Tasting,
    @Relation(
        parentColumn = "id",
        entityColumn = "tasting_id"
    )
    var bottles: List<Bottle>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TastingXFriend::class,
            parentColumn = "tasting_id",
            entityColumn = "friend_id",
        )
    )
    val friends: List<Friend>
)
