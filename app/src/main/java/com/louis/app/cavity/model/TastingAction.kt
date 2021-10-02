package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val type: Action,
    val time: Int,
    @ColumnInfo(name = "bottle_id") val bottleId: Long,
    var checked: Int
) {
    enum class Action {
        OUT_OF_FRIDGE,
        OUT_OF_CELLAR,
        SET_TO_FRIDGE,
        SET_TO_JUG
    }
}
