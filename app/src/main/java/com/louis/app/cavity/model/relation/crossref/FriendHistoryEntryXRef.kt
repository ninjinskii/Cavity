package com.louis.app.cavity.model.relation.crossref

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.HistoryEntry

@Entity(
    tableName = "friend_history_entry_xref",
    primaryKeys = ["friend_id", "history_entry_id"],
    foreignKeys = [
        ForeignKey(
            entity = Friend::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("friend_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = HistoryEntry::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("history_entry_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FriendHistoryEntryXRef(
    @ColumnInfo(name = "history_entry_id") val historyEntryId: Long,
    @ColumnInfo(name = "friend_id") val friendId: Long
)
