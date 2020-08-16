package com.louis.app.cavity.model

import androidx.room.*

@Entity(tableName = "counties")
data class County(val name: String, @ColumnInfo(name = "pref_order") val prefOrder: Int) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_county")
    var idCounty: Long = 0
}