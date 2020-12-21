package com.louis.app.cavity.model.relation

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "q_grape", primaryKeys = ["bottle_id", "grape_id"])
data class QuantifiedBottleGrapeXRef(
    @ColumnInfo(name = "bottle_id") val bottleId: Long,
    @ColumnInfo(name = "grape_id") val grapeId: Long,
    var percentage: Int,
)
