package com.louis.app.cavity.model.relation.tasting

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Tasting

data class TastingWithBottles(
    @Embedded val tasting: Tasting,
    @Relation(
        parentColumn = "tasting_id",
        entityColumn = "bottle_id"
    )
    val bottles: List<Bottle>
)
