package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.louis.app.cavity.model.Person
import com.louis.app.cavity.model.Tasting


data class TastingWithPersons(
    @Embedded val tasting: Tasting,
    @Relation(
        parentColumn = "id_tasting",
        entityColumn = "id_person",
        associateBy = Junction(TastingPersonXRef::class)
    )
    val persons: List<Person>
)