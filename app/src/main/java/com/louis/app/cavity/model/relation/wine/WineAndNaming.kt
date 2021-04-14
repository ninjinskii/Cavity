package com.louis.app.cavity.model.relation.wine

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Naming
import com.louis.app.cavity.model.Wine

data class WineAndNaming(
    @Embedded val wine: Wine,
    @Relation(
        parentColumn = "naming_id",
        entityColumn = "id"
    )
    val naming: Naming
)
