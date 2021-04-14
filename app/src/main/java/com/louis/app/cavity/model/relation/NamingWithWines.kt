package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Naming
import com.louis.app.cavity.model.Wine

data class NamingWithWines(
    @Embedded val naming: Naming,
    @Relation(
        parentColumn = "id",
        entityColumn = "naming_id"
    )
    val winesWines: List<Wine>
)
