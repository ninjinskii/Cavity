package com.louis.app.cavity.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "grape", indices = [Index(value = ["name"], unique = true)])
data class Grape(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String
) : Chipable {
    override fun getItemId() = id
    override fun getChipText() = name
}
