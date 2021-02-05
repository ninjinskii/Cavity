package com.louis.app.cavity.model.relation.bottle

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.relation.crossref.FilledBottleReviewXRef
import com.louis.app.cavity.model.relation.crossref.QuantifiedBottleGrapeXRef
import java.util.*

data class BottleAndWineWithQGrapesAndFReviews(
    @Embedded val bottle: Bottle,
    @Relation(
        parentColumn = "wine_id",
        entityColumn = "id"
    )
    val wine: Wine,
    @Relation(
        entity = QuantifiedBottleGrapeXRef::class,
        parentColumn = "id",
        entityColumn = "bottle_id",
        projection = ["id"]
    )
    val qGrapes: List<QuantifiedBottleGrapeXRef>,
    @Relation(
        entity = FilledBottleReviewXRef::class,
        parentColumn = "id",
        entityColumn = "bottle_id",
        projection = ["id"]
    )
    val fReviews: List<FilledBottleReviewXRef>,
) {
    fun isReadyToDrink(): Boolean {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        return year >= bottle.apogee
    }
}
