package com.louis.app.cavity.domain.repository

import android.app.Application
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.domain.repository.RepositoryUpsertResult.*
import com.louis.app.cavity.domain.repository.RepositoryUpsertResult.Companion.handleDatabaseError

class FriendRepository private constructor(app: Application) : Repository(app) {
    companion object {
        @Volatile
        var instance: FriendRepository? = null

        fun getInstance(app: Application) =
            instance ?: synchronized(this) {
                instance ?: FriendRepository(app).also { instance = it }
            }
    }

    private val friendDao = database.friendDao()

    suspend fun insertFriend(friend: Friend): RepositoryUpsertResult<Long> {
        if (!friend.hasValidName()) {
            return InvalidName
        }

        try {
            val friendId = friendDao.insertFriend(friend)
            return Success(friendId)
        } catch (e: Exception) {
            return handleDatabaseError(e, errorReporter)
        }
    }

    suspend fun insertFriends(friends: List<Friend>) = friendDao.insertFriends(friends)

    suspend fun updateFriend(friend: Friend): RepositoryUpsertResult<Long> {
        if (!friend.hasValidName()) {
            return InvalidName
        }

        try {
            friendDao.updateFriend(friend)
            return Success(friend.id)
        } catch (e: Exception) {
            return handleDatabaseError(e, errorReporter)
        }
    }

    suspend fun deleteFriend(friend: Friend) = friendDao.deleteFriend(friend)

    suspend fun deleteAllFriends() = friendDao.deleteAll()

    fun getAllFriends() = friendDao.getAllFriends()

    suspend fun getFriendByIdNotLive(friendId: Long) = friendDao.getFriendByIdNotLive(friendId)

    suspend fun getAllFriendsNotLive() = friendDao.getAllFriendsNotLive()
}
