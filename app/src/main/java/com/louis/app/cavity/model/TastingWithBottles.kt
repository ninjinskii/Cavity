package com.louis.app.cavity.model

import androidx.room.Embedded
import androidx.room.Relation

data class TastingWithBottles (
    @Embedded val tasting: Tasting,
    @Relation(
        parentColumn = "id_tasting",
        entityColumn = "id_bottle"
    )
    val bottles: List<Bottle>
)