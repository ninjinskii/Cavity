package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Grape

data class QuantifiedGrapeAndGrape(
    @Embedded val qGrape: QuantifiedBottleGrapeXRef,
    @Relation(
        parentColumn = "grape_id",
        entityColumn = "id"
    )
    val grape: Grape
) {
    fun getId() = "${qGrape.grapeId}${qGrape.bottleId}".toLong()
}
