package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grape")
data class Grape(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_grape")
    val idGrape: Long = 0,
    val name: String,
    val percentage: Int,
    @ColumnInfo(name = "id_bottle") val idBottle: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Grape

        if (idGrape != other.idGrape) return false
        if (name != other.name) return false
        if (percentage != other.percentage) return false
        if (idBottle != other.idBottle) return false

        return true
    }

    override fun hashCode(): Int {
        var result = idGrape.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + percentage
        result = 31 * result + idBottle.hashCode()
        return result
    }
}