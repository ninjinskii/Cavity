package com.louis.app.cavity.model.relation.bottle

import androidx.room.Embedded
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
        entity = QuantifiedBottleGrapeXRef::class,
        parentColumn = "id",
        entityColumn = "bottle_id"
    )
    val grapes: List<Grape>,
    @Relation(
        entity = FilledBottleReviewXRef::class,
        parentColumn = "id",
        entityColumn = "bottle_id"
        //associateBy = ?
    )
    val reviews: List<Review>,
)
