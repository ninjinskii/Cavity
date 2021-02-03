package com.louis.app.cavity.model.relation.review

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.model.relation.crossref.FilledBottleReviewXRef

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
