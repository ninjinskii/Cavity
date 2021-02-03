package com.louis.app.cavity.model.relation.crossref

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["friend_id", "history_entry_id"])
data class FriendHistoryEntryXRef(
    @ColumnInfo(name = "friend_id") val friendId: Long,
    @ColumnInfo(name = "history_entry_id") val historyEntryId: Long
)
