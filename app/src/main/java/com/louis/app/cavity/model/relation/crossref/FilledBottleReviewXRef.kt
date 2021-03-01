package com.louis.app.cavity.model.relation.crossref

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Review

@Entity(
    tableName = "f_review",
    primaryKeys = ["bottle_id", "review_id"],
    foreignKeys = [
        ForeignKey(
            entity = Bottle::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("bottle_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Review::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("review_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FilledBottleReviewXRef(
    @ColumnInfo(name = "bottle_id") val bottleId: Long,
    @ColumnInfo(name = "review_id", index = true) val reviewId: Long,
    var value: Int
)
