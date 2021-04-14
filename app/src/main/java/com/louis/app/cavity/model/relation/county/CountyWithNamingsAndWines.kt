package com.louis.app.cavity.model.relation.county

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Naming
import com.louis.app.cavity.model.relation.NamingWithWines

data class CountyWithNamingsAndWines(
    @Embedded val county: County,
    @Relation(
        entity = Naming::class,
        parentColumn = "id",
        entityColumn = "county_id"
    )
    val namingsWithWines: List<NamingWithWines>
)
