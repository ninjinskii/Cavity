package com.louis.app.cavity.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class HistoryEntryWithPersons(
    @Embedded val historyEntry: HistoryEntry,
    @Relation(
        parentColumn = "id_history_entry",
        entityColumn = "id_person",
        associateBy = Junction(PersonHistoryEntryXRef::class)
    )
    val persons: List<Person>
)
