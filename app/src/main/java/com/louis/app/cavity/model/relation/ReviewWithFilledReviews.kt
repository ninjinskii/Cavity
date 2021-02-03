package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Review

data class ReviewWithFilledReviews(
    @Embedded val review: Review,
    @Relation(
        parentColumn = "id",
        entityColumn = "review_id"
    )
    val fReview: List<FilledBottleReviewXRef>
)
