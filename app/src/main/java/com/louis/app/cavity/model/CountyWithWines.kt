package com.louis.app.cavity.model

import androidx.room.Embedded
import androidx.room.Relation

data class CountyWithWines (
    @Embedded val county: County,
    @Relation(
        parentColumn = "id_county",
        entityColumn = "id_wine"
    )
    val wines: List<Wine>
)