package com.louis.app.cavity.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.louis.app.cavity.model.TastingXFriend

@Dao
interface TastingXFriendDao {
    @Insert
    suspend fun insertTastingXFriend(tasting: TastingXFriend)

    @Update
    suspend fun updateTastingXFriend(tasting: TastingXFriend)

    @Delete
    suspend fun deleteTastingXFriend(tasting: TastingXFriend)
}
