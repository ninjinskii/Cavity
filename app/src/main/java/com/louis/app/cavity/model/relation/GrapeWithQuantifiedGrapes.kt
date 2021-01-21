package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Grape

data class GrapeWithQuantifiedGrapes(
        @Embedded val grape: Grape,
        @Relation(
                parentColumn = "grape_id",
                entityColumn = "grape_id"
        )
        val qGrapes: List<QuantifiedBottleGrapeXRef>
)
