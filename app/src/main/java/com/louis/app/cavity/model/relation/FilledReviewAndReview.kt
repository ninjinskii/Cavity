package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Review

data class FilledReviewAndReview(
    @Embedded val fReview: FilledBottleReviewXRef,

    @Relation(
        parentColumn = "review_id",
        entityColumn = "id"
    )
    val review: Review
) {
    fun getId() = "${fReview.reviewId}${fReview.bottleId}".toLong()
}
