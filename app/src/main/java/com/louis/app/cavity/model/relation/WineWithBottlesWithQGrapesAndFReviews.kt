package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Wine


data class WineWithBottlesWithQGrapesAndFReviews(
    @Embedded val wine: Wine,
    @Relation(
        entity = Bottle::class,
        parentColumn = "wineId",
        entityColumn = "wineId",
    )
    val bottles: List<BottleWithQGrapesAndFReviews>
)
