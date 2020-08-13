package com.louis.app.cavity.model

import androidx.room.Embedded
import androidx.room.Relation

data class WineWithBottles (
    @Embedded val wine: Wine,
    @Relation(
        parentColumn = "id_wine",
        entityColumn = "id_bottle"
    )
    val bottle: List<Bottle>
)