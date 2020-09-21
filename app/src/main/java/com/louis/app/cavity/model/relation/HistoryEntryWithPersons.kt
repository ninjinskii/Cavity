package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.Person

data class HistoryEntryWithPersons(
    @Embedded val historyEntry: HistoryEntry,
    @Relation(
        parentColumn = "history_entry_id",
        entityColumn = "person_id",
        associateBy = Junction(PersonHistoryEntryXRef::class)
    )
    val persons: List<Person>
)
