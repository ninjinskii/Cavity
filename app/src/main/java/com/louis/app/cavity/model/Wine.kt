package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "wine")
data class Wine(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_wine")
    val idWine: Long = 0,
    val name: String,
    val naming: String,
    val color: Int,
    val cuvee: String,
    @ColumnInfo(name = "id_county") val idCounty: Long,
    @ColumnInfo(name = "is_bio") val isBio: Int,
    @ColumnInfo(name = "img_path") val imgPath: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Wine

        if (idWine != other.idWine) return false
        if (name != other.name) return false
        if (naming != other.naming) return false
        if (color != other.color) return false
        if (cuvee != other.cuvee) return false
        if (idCounty != other.idCounty) return false
        if (isBio != other.isBio) return false
        if (imgPath != other.imgPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = idWine.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + naming.hashCode()
        result = 31 * result + color
        result = 31 * result + cuvee.hashCode()
        result = 31 * result + idCounty.hashCode()
        result = 31 * result + isBio
        result = 31 * result + imgPath.hashCode()
        return result
    }
}