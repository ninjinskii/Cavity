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
    @ColumnInfo(name = "bottle_id", index = true) val bottleId: Long,
    var done: Int
) {
    enum class Action {
        SET_TO_FRIDGE,
        SET_TO_JUG,
        UNCORK
    }
}
