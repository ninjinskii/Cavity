package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Wine

data class CountyWithWines(
    @Embedded val county: County,
    @Relation(
        parentColumn = "county_id",
        entityColumn = "county_id"
    )
    val wines: List<Wine>
)
