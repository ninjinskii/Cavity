package com.louis.app.cavity.model.relation.review

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.model.relation.crossref.FilledBottleReviewXRef

data class FilledReviewAndReview(
    @Embedded val fReview: FilledBottleReviewXRef,
    @Relation(
        entity = Review::class,
        parentColumn = "review_id",
        entityColumn = "id",
        projection = ["contest_name"]
    )
    val contestName: String
) {
    fun getId() = "${fReview.reviewId}${fReview.bottleId}".toLong()
}
