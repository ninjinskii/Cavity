package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasting_entry")
data class TastingEntry (
    @ColumnInfo(name = "wine_temp") val wineTemp: Int = -1,
    @ColumnInfo(name = "cellar_temp") val cellarTemp: Int = -1,
    @ColumnInfo(name = "fridge_temp") val fridgeTemp: Int = -1,
    @ColumnInfo(name = "id_bottle") val idBottle: Long,
    @ColumnInfo(name = "id_tasting") val idTasting: Long
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_tasting_entry")
    var idTastingEntry: Long = 0
}