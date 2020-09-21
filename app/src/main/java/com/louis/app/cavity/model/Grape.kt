package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grape")
data class Grape(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "grape_id")
    val grapeId: Long = 0,
    val name: String,
    var percentage: Int,
    @ColumnInfo(name = "bottle_id") var bottleId: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Grape

        if (grapeId != other.grapeId) return false
        if (name != other.name) return false
        if (percentage != other.percentage) return false
        if (bottleId != other.bottleId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = grapeId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + percentage
        result = 31 * result + bottleId.hashCode()
        return result
    }
}
