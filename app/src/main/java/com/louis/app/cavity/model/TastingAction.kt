package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "tasting_action",
    foreignKeys = [ForeignKey(
        entity = Bottle::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("bottle_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class TastingAction(
    val id: Long,
    @ColumnInfo(name = "bottle_id") val bottleId: Long,
    val checked: Boolean
)
