package com.louis.app.cavity.model.relation

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["person_id", "history_entry_id"])
data class PersonHistoryEntryXRef(
    @ColumnInfo(name = "person_id") val idPerson: Long,
    @ColumnInfo(name = "history_entry_id") val idHistoryEntry: Long
)
