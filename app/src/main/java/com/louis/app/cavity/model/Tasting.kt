package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasting")
data class Tasting(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val opportunity: String,
    @ColumnInfo(name = "cellar_temp") val cellarTemp: Int,
    @ColumnInfo(name = "fridge_temp") val fridgeTemp: Int,
    @ColumnInfo(name = "freezer_temp") val freezerTemp: Int,
    @ColumnInfo(name = "ambient_temp") val ambientTemp: Int = Temperature.DEFAULT_DAY_TEMP,
    @ColumnInfo(name = "is_default_ambient_temp") val isDefaultAmbientTemp: Boolean = true, // Used to suggest user to inquire temp ambient in his home the d day
)
