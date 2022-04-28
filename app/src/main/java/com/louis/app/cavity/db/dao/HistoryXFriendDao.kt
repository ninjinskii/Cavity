package com.louis.app.cavity.db.dao

import androidx.room.*
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

    @Query("SELECT * FROM friend_history_entry_xref")
    suspend fun getAllHistoryXFriendsNotLive(): List<HistoryXFriend>
}
