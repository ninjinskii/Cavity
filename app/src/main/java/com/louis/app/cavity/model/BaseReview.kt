package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class BaseReview (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "base_review_id")
    val baseReviewId: Long = 0,
    val name: String,
    @ColumnInfo(name = "is_medal") var isMedal: Int,
    @ColumnInfo(name = "is_star") var isStar: Int,
    @ColumnInfo(name = "is_rate_20") var isRate20: Int,
    @ColumnInfo(name = "is_rate_100") var isRate100: Int,
)
