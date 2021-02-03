package com.louis.app.cavity.model.relation.review

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.model.relation.crossref.FilledBottleReviewXRef

data class ReviewWithFilledReviews(
    @Embedded val review: Review,

    @Relation(
        parentColumn = "id",
        entityColumn = "review_id"
    )
    val fReview: List<FilledBottleReviewXRef>
)
