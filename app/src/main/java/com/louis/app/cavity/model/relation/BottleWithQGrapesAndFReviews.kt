package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle

class BottleWithQGrapesAndFReviews(
    @Embedded val bottle: Bottle,
    @Relation(
        parentColumn = "bottle_id",
        entityColumn = "bottle_id"
    )
    val grapes: List<QuantifiedBottleGrapeXRef>,
    @Relation(
        parentColumn = "bottle_id",
        entityColumn = "bottle_id"
    )
    val reviews: List<FilledBottleReviewXRef>
)
