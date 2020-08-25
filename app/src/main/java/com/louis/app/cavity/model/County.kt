package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "county")
data class County(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_county")
    val idCounty: Long = 0,
    val name: String,
    @ColumnInfo(name = "pref_order") val prefOrder: Int
)