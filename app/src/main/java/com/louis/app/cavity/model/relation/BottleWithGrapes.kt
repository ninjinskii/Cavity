package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Grape

data class BottleWithGrapes (
    @Embedded val bottle: Bottle,
    @Relation(
        parentColumn = "bottle_id",
        entityColumn = "bottle_id"
    )
    val grapes: List<Grape>
)
