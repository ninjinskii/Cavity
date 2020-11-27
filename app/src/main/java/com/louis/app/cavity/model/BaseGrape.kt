package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class BaseGrape(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "base_grape_id")
    val baseGrapeId: Long = 0,
    val name: String,
)
