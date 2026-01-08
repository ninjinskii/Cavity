package com.louis.app.cavity.model

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(
    tableName = "tag_bottle_xref",
    primaryKeys = ["tag_id", "bottle_id"],
    foreignKeys = [
        ForeignKey(
            entity = Tag::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("tag_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Bottle::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("bottle_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TagXBottle (
    @ColumnInfo(name = "tag_id", index = true) val tagId: Long,
    @ColumnInfo(name = "bottle_id", index = true) val bottleId: Long
)
