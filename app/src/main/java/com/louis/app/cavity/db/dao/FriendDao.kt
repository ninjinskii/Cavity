package com.louis.app.cavity.db.dao

import androidx.room.*
import com.louis.app.cavity.model.Friend
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Insert
    suspend fun insertFriend(friend: Friend): Long

    @Insert
    suspend fun insertFriends(friends: List<Friend>)

    @Update
    suspend fun updateFriend(friend: Friend)

    @Delete
    suspend fun deleteFriend(friend: Friend)

    @Query("SELECT * FROM friend ORDER BY name")
    fun getAllFriends(): Flow<List<Friend>>

    @Query("SELECT * FROM friend WHERE id=:friendId")
    suspend fun getFriendByIdNotLive(friendId: Long): Friend

    @Query("SELECT * FROM friend")
    suspend fun getAllFriendsNotLive(): List<Friend>

    @Query("DELETE FROM friend")
    suspend fun deleteAll()
}
