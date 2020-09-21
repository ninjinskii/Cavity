package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasting_entry")
data class TastingEntry(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "tasting_entry_id")
    val tastingEntryId: Long = 0,
    @ColumnInfo(name = "wine_temp") val wineTemp: Int = -1,
    @ColumnInfo(name = "cellar_temp") val cellarTemp: Int = -1,
    @ColumnInfo(name = "fridge_temp") val fridgeTemp: Int = -1,
    @ColumnInfo(name = "bottle_id") val bottleId: Long,
    @ColumnInfo(name = "tasting_id") val tastingId: Long
)
