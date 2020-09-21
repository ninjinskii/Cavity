package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_entry")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "history_entry_id")
    val historyEntryId: Long = 0,
    @ColumnInfo(name = "drink_date") val drinkDate: String,
    //@ColumnInfo(name = "id_bottle") val idBottle: Long
)
