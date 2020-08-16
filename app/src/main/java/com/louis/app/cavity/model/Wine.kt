package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wine")
data class Wine(
    val name: String,
    val naming: String,
    @ColumnInfo(name = "id_county") val idCounty: Long,
    @ColumnInfo(name = "is_bio") val isBio: Int
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_wine")
    var idWine: Long = 0
}