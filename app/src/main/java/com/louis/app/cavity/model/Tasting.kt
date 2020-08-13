package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tasting(val date: String) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_tasting")
    var idTasting: Long = 0
}