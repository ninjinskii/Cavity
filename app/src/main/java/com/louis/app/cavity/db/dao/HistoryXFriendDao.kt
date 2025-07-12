package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.HistoryXFriend

@Dao
interface HistoryXFriendDao {
    @Insert
    suspend fun insertHistoryXFriend(fxh: HistoryXFriend)

    @Insert
    suspend fun insertHistoryXFriends(fxh: List<HistoryXFriend>)

    @Update
    suspend fun updateHistoryXFriend(fxh: HistoryXFriend)

    @Delete
    suspend fun deleteHistoryXFriend(fxh: HistoryXFriend)

    @Query("SELECT * FROM friend_history_entry_xref")
    suspend fun getAllHistoryXFriendsNotLive(): List<HistoryXFriend>

    @Transaction
    @Query(
        """
        SELECT *, COUNT(*) AS count
        FROM friend
        LEFT JOIN friend_history_entry_xref ON friend.id = friend_history_entry_xref.friend_id
        GROUP BY id
        ORDER BY count DESC
    """
    )
    fun getFriendSortedByFrequence(): LiveData<List<Friend>>

    @Query("DELETE FROM friend_history_entry_xref")
    suspend fun deleteAll()
}
