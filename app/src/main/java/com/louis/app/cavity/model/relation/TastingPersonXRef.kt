package com.louis.app.cavity.model.relation

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["tasting_id", "person_id"])
data class TastingPersonXRef(
    @ColumnInfo(name = "tasting_id") val idTasting: Long,
    @ColumnInfo(name = "person_id") val idPerson: Long
)
