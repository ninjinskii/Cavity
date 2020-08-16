package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity
data class Wine(val name: String, val naming: String, val countyId: Long) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_wine")
    var idWine: Long = 0
}