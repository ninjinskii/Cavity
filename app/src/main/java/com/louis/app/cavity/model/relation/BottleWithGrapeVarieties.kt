package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.GrapeVariety

data class BottleWithGrapeVarieties (
    @Embedded val bottle: Bottle,
    @Relation(
        parentColumn = "id_bottle",
        entityColumn = "id_grape_variety"
    )
    val grapeVarieties: List<GrapeVariety>
)
