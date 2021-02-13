package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_entry")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "bottle_id") val bottleId: Long,
    @ColumnInfo(name = "tasting_id") var tastingId: Long? = null,
    val comment: String,
    val type: HistoryEntryType,
)
