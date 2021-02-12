package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "review", indices = [Index(value = ["contest_name"], unique = true)])
data class Review(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "contest_name") val contestName: String,
    @ColumnInfo(name = "type") var type: Int,
) : Chipable {
    override fun getItemId() = id
    override fun getChipText() = contestName
}
