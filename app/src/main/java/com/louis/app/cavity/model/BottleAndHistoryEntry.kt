package com.louis.app.cavity.model

import androidx.room.Embedded
import androidx.room.Relation


data class BottleAndHistoryEntry (
    @Embedded val bottle: Bottle,
    @Relation(
        parentColumn = "id_bottle",
        entityColumn = "id_bottle"
    )
    val historyEntry: HistoryEntry
)