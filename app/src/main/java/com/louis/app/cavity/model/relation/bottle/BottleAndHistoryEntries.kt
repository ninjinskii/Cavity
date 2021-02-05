package com.louis.app.cavity.model.relation.bottle

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.HistoryEntry


data class BottleAndHistoryEntries(
    @Embedded val bottle: Bottle,
    @Relation(
        parentColumn = "id",
        entityColumn = "bottle_id"
    )
    val historyEntries: List<HistoryEntry>
)
