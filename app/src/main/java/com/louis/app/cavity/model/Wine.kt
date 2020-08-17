package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "wine")
data class Wine(
    val name: String,
    val naming: String,
    val color: Int,
    @ColumnInfo(name = "id_county") val idCounty: Long,
    @ColumnInfo(name = "is_bio") val isBio: Int,
    @ColumnInfo(name = "img_path") val imgPath: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_wine")
    var idWine: Long = 0
    @Ignore var childBottlesVintages: MutableList<Int> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Wine

        if (name != other.name) return false
        if (naming != other.naming) return false
        if (color != other.color) return false
        if (idCounty != other.idCounty) return false
        if (isBio != other.isBio) return false
        if (idWine != other.idWine) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + naming.hashCode()
        result = 31 * result + color
        result = 31 * result + idCounty.hashCode()
        result = 31 * result + isBio
        result = 31 * result + idWine.hashCode()
        return result
    }

}