package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.louis.app.cavity.util.toInt

@Entity(tableName = "review")
data class Review(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "review_id")
    val reviewId: Long = 0,
    @ColumnInfo(name = "base_review_id") val baseReviewId: Long,
    @ColumnInfo(name = "contest_name") val contestName: String,
    val value: Int,
    @ColumnInfo(name = "bottle_id") var bottleId: Long
)
