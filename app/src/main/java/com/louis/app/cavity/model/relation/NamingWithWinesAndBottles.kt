package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Naming
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.relation.wine.WineWithBottles

data class NamingWithWinesAndBottles(
    @Embedded val naming: Naming,
    @Relation(
        entity = Wine::class,
        parentColumn = "id",
        entityColumn = "naming_id"
    )
    val winesWithBottles: List<WineWithBottles>
)
