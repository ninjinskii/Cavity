package com.louis.app.cavity.model.relation.county

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Wine

data class CountyWithWines(
    @Embedded val county: County,
    @Relation(
        parentColumn = "id",
        entityColumn = "county_id"
    )
    val wines: List<Wine>
)
