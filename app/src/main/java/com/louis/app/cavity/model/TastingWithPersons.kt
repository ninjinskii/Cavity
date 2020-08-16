package com.louis.app.cavity.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation


data class TastingWithPersons(
    @Embedded val tasting: Tasting,
    @Relation(
        parentColumn = "id_tasting",
        entityColumn = "id_person",
        associateBy = Junction(TastingPersonXRef::class)
    )
    val persons: List<Person>
)