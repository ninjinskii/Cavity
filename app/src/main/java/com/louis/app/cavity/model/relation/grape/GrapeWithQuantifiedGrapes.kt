package com.louis.app.cavity.model.relation.grape

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.relation.crossref.QuantifiedBottleGrapeXRef

data class GrapeWithQuantifiedGrapes(
    @Embedded val grape: Grape,
    @Relation(
        parentColumn = "id",
        entityColumn = "grape_id"
    )
    val qGrapes: List<QuantifiedBottleGrapeXRef>
)
