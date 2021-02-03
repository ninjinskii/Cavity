package com.louis.app.cavity.model.relation.crossref

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Grape

@Entity(
    tableName = "q_grape",
    primaryKeys = ["bottle_id", "grape_id"],
    foreignKeys = [
        ForeignKey(
            entity = Bottle::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("bottle_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Grape::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("grape_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuantifiedBottleGrapeXRef(
    @ColumnInfo(name = "bottle_id") val bottleId: Long,
    @ColumnInfo(name = "grape_id") val grapeId: Long,
    var percentage: Int,
)
