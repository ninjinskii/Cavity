package com.louis.app.cavity.model.relation.bottle

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.relation.crossref.FilledBottleReviewXRef
import com.louis.app.cavity.model.relation.crossref.QuantifiedBottleGrapeXRef
import com.louis.app.cavity.model.relation.wine.WineAndNaming

data class BoundedBottle(
    @Embedded val bottle: Bottle,
    @Relation(
        entity = Wine::class,
        parentColumn = "wine_id",
        entityColumn = "id"
    )
    val wineAndNaming: WineAndNaming,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = QuantifiedBottleGrapeXRef::class,
            parentColumn = "bottle_id",
            entityColumn = "grape_id"
        )
    )
    val grapes: List<Grape>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = FilledBottleReviewXRef::class,
            parentColumn = "bottle_id",
            entityColumn = "review_id"
        )
    )
    val reviews: List<Review>,
)
