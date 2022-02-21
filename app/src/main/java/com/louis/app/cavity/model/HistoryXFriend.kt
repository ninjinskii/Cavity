package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

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
data class HistoryXFriend(
    @ColumnInfo(name = "history_entry_id", index = true) val historyEntryId: Long,
    @ColumnInfo(name = "friend_id", index = true) val friendId: Long
)
