package com.louis.app.cavity.model.relation

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "f_review", primaryKeys = ["bottle_id", "review_id"])
data class FilledBottleReviewXRef(
    @ColumnInfo(name = "bottle_id") val bottleId: Long,
    @ColumnInfo(name = "review_id") val reviewId: Long,
    var value: Int
)
