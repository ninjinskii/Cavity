package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "history_entry",
    foreignKeys = [ForeignKey(
        entity = Bottle::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("bottle_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "bottle_id") val bottleId: Long,
    @ColumnInfo(name = "tasting_id") var tastingId: Long? = null,
    val comment: String,
    val type: HistoryEntryType,
    val favorite: Int,
)
