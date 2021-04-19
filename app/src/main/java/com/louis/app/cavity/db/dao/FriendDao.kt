package com.louis.app.cavity.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.louis.app.cavity.model.Friend

@Dao
interface FriendDao {
    @Insert
    suspend fun insertFriend(friend: Friend)

    @Update
    suspend fun updateFriend(friend: Friend)

    @Delete
    suspend fun deleteFriend(friend: Friend)

    @Query("SELECT * FROM friend")
    fun getAllFriends(): LiveData<List<Friend>>

    @Query("SELECT * FROM friend")
    suspend fun getAllFriendsNotLive(): List<Friend>
}