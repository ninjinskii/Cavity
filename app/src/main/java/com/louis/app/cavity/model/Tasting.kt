package com.louis.app.cavity.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasting")
data class Tasting(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val isMidday: Boolean,
    val opportunity: String,
    var done: Boolean = false
)
