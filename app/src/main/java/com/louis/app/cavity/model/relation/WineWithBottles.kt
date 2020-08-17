package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Wine

data class WineWithBottles (
    @Embedded val wine: Wine,
    @Relation(
        parentColumn = "id_wine",
        entityColumn = "id_bottle"
    )
    val bottles: List<Bottle>
)