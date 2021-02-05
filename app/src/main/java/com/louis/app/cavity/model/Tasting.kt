package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasting")
data class Tasting(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    @ColumnInfo(name = "wine_temp") val wineTemp: Int = -1,
    @ColumnInfo(name = "cellar_temp") val cellarTemp: Int = -1,
    @ColumnInfo(name = "fridge_temp") val fridgeTemp: Int = -1,
    @ColumnInfo(name = "bottle_id") val bottleId: Long,
    @ColumnInfo(name = "tasting_id") val tastingId: Long
)
