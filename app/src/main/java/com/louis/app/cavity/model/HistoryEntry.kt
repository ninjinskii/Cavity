package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_entry")
data class HistoryEntry(
    @ColumnInfo(name = "drink_date") val drinkDate: String,
    @ColumnInfo(name = "id_bottle") val idBottle: Long
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_history_entry")
    var idHistoryEntry: Long = 0
}