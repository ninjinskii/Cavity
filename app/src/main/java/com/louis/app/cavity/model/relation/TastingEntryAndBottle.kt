package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.TastingEntry

data class TastingEntryAndBottle (
    @Embedded val tastingEntry: TastingEntry,
    @Relation(
        parentColumn = "tasting_entry_id",
        entityColumn = "bottle_id"
    )
    val bottle: Bottle
)
