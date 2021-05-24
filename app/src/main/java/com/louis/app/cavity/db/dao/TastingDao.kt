package com.louis.app.cavity.db.dao

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
