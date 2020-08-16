package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.Person

data class HistoryEntryWithPersons(
    @Embedded val historyEntry: HistoryEntry,
    @Relation(
        parentColumn = "id_history_entry",
        entityColumn = "id_person",
        associateBy = Junction(PersonHistoryEntryXRef::class)
    )
    val persons: List<Person>
)
