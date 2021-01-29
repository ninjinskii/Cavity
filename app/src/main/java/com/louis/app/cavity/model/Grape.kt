package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grape")
data class Grape(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "grape_id")
    val grapeId: Long,
    val name: String
) : Chipable {
    override fun getId() = grapeId
    override fun getChipText() = name
}
