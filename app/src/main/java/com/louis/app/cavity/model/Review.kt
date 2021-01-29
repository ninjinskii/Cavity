package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review")
data class Review(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "review_id")
    val reviewId: Long = 0,
    @ColumnInfo(name = "contest_name") val contestName: String,
    @ColumnInfo(name = "type") var type: Int,
) : Chipable {
    override fun getId() = reviewId
    override fun getChipText() = contestName
}
