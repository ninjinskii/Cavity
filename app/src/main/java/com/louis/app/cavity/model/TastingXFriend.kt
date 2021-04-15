package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "tasting_friend_xref",
    primaryKeys = ["tasting_id", "friend_id"],
    foreignKeys = [
        ForeignKey(
            entity = Tasting::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("tasting_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Friend::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("friend_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TastingXFriend(
    @ColumnInfo(name = "tasting_id") val tastingId: Long,
    @ColumnInfo(name = "friend_id") val friendId: Long
)
