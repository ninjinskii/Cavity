package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.TastingEntry

data class TastingEntryAndBottle (
    @Embedded val tastingEntry: TastingEntry,
    @Relation(
        parentColumn = "id_tasting_entry",
        entityColumn = "id_bottle"
    )
    val bottle: Bottle
)