package com.louis.app.cavity.model.relation.wine

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle

data class WineWithBottles(
    @Embedded val wine: WineAndNaming,
    @Relation(
        parentColumn = "id",
        entityColumn = "wine_id"
    )
    val bottles: List<Bottle>
)
