package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasting")
data class Tasting(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_tasting")
    val idTasting: Long = 0,
    val date: String
)