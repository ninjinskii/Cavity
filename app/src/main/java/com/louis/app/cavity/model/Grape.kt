package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grape")
data class Grape(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "grape_id")
    val grapeId: Long = 0,
    val name: String,
    var percentage: Int,
    @ColumnInfo(name = "bottle_id") var bottleId: Long
)
