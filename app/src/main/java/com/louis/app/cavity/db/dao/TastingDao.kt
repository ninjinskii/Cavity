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
    suspend fun insertTasting(tasting: Tasting)

    @Update
    suspend fun updateTasting(tasting: Tasting)

    @Delete
    suspend fun deleteTasting(tasting: Tasting)

    @Transaction
    @Query("SELECT * FROM tasting WHERE date >= :beyond")
    fun getFutureTastings(beyond: Long = 0L): LiveData<List<BoundedTasting>>
}

data class TastingWithBottles(
    @Embedded var tasting: Tasting,
    @Relation(
        parentColumn = "id",
        entityColumn = "tasting_id"
    )
    var bottles: List<Bottle>
)

data class TastingWithFriends(
    @Embedded val tasting: Tasting,
    @Relation(
        parentColumn = "id",
        entityColumn = "tasting_id",
        associateBy = Junction(TastingXFriend::class)
    )
    val friends: List<Friend>
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
