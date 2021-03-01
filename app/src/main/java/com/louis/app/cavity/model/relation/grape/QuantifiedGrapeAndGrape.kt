package com.louis.app.cavity.model.relation.grape

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.relation.crossref.QuantifiedBottleGrapeXRef

data class QuantifiedGrapeAndGrape(
    @Embedded val qGrape: QuantifiedBottleGrapeXRef,
    @Relation(
        entity = Grape::class,
        parentColumn = "grape_id",
        entityColumn = "id",
        projection = ["name"]
    )
    val grapeName: String
) {
    fun getId() = "${qGrape.grapeId}${qGrape.bottleId}".toLong()
}
