package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.HistoryEntry


data class BottleAndHistoryEntry (
    @Embedded val bottle: Bottle,
    @Relation(
        parentColumn = "bottle_id",
        entityColumn = "history_entry_id"
    )
    val historyEntry: HistoryEntry
)
