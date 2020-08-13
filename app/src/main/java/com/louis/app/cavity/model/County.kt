package com.louis.app.cavity.model

import androidx.room.*

@Entity(tableName = "counties")
data class County(var name: String, @ColumnInfo(name = "pref_order") var prefOrder: Int) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_county")
    var idCounty: Long = 0
}