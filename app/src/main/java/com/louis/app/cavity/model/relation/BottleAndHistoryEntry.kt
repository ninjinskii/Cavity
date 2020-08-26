package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.HistoryEntry


data class BottleAndHistoryEntry (
    @Embedded val bottle: Bottle,
    @Relation(
        parentColumn = "id_bottle",
        entityColumn = "id_history_entry"
    )
    val historyEntry: HistoryEntry
)