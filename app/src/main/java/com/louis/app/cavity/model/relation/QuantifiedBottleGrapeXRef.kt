package com.louis.app.cavity.model.relation

import androidx.room.Entity

@Entity(primaryKeys = ["bottle_id", "grape_id"])
data class QuantifiedBottleGrapeXRef(
    val bottleId: Long,
    val grapeId: Long,
    val percentage: Int,
)
