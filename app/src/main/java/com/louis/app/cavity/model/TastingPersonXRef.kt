package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["id_tasting", "id_person"])
data class TastingPersonXRef(
    @ColumnInfo(name = "id_tasting") val idTasting: Long,
    @ColumnInfo(name = "id_person") val idPerson: Long
)