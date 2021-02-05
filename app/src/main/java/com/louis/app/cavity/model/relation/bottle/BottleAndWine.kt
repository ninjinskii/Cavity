package com.louis.app.cavity.model.relation.bottle

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Wine

class BottleAndWine(
    @Embedded val bottle: Bottle,
    @Relation(
        parentColumn = "wine_id",
        entityColumn = "id"
    )
    val wine: Wine,
)
