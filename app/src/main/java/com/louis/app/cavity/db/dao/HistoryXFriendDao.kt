package com.louis.app.cavity.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.louis.app.cavity.model.HistoryXFriend

@Dao
interface HistoryXFriendDao {
    @Insert
    suspend fun insertHistoryXFriend(fxh: HistoryXFriend)

    @Insert
    suspend fun insertHistoryXFriend(fxh: List<HistoryXFriend>)

    @Update
    suspend fun updateHistoryXFriend(fxh: HistoryXFriend)

    @Delete
    suspend fun deleteHistoryXFriend(fxh: HistoryXFriend)
}
