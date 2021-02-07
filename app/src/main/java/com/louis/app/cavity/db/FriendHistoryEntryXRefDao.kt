package com.louis.app.cavity.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import com.louis.app.cavity.model.relation.crossref.FriendHistoryEntryXRef
import com.louis.app.cavity.model.relation.crossref.TastingFriendXRef

@Dao
interface FriendHistoryEntryXRefDao {
    @Insert
    suspend fun insertFriendHistoryEntryXRef(fxh: FriendHistoryEntryXRef)

    @Update
    suspend fun updateFriendHistoryEntryXRef(fxh: FriendHistoryEntryXRef)

    @Delete
    suspend fun deleteFriendHistoryEntryXRef(fxh: FriendHistoryEntryXRef)
}
