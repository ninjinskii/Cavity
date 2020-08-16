package com.louis.app.cavity.model.relation

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["id_person", "id_history_entry"])
data class PersonHistoryEntryXRef(
    @ColumnInfo(name = "id_person") val idPerson: Long,
    @ColumnInfo(name = "id_history_entry") val idHistoryEntry: Long
)