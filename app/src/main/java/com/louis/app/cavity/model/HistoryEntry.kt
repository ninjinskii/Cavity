package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HistoryEntry(
    @Embedded var bottle: Bottle,
    @ColumnInfo(name = "drink_date") var drinkDate: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_history_entry")
    var idHistoryEntry: Long = 0
}