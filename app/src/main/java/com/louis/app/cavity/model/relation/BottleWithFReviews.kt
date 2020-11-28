package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Review

data class BottleWithFReviews(
    @Embedded val bottle: Bottle,
    @Relation(
        parentColumn = "bottle_id",
        entityColumn = "bottle_id"
    )
    val reviews: List<Review> // TODO: change to FilledBottleReviewXRef
)
