package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

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
data class QGrape(
    @ColumnInfo(name = "bottle_id") val bottleId: Long,
    @ColumnInfo(name = "grape_id", index = true) val grapeId: Long,
    var percentage: Int,
)
