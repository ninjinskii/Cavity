package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wine")
data class Wine(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "wine_id")
    var wineId: Long = 0,
    val name: String,
    val naming: String,
    val color: Int,
    val cuvee: String,
    @ColumnInfo(name = "county_id") val countyId: Long,
    @ColumnInfo(name = "is_organic") val isOrganic: Int,
    @ColumnInfo(name = "img_path") val imgPath: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Wine

        if (wineId != other.wineId) return false
        if (name != other.name) return false
        if (naming != other.naming) return false
        if (color != other.color) return false
        if (cuvee != other.cuvee) return false
        if (countyId != other.countyId) return false
        if (isOrganic != other.isOrganic) return false
        if (imgPath != other.imgPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = wineId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + naming.hashCode()
        result = 31 * result + color
        result = 31 * result + cuvee.hashCode()
        result = 31 * result + countyId.hashCode()
        result = 31 * result + isOrganic
        result = 31 * result + imgPath.hashCode()
        return result
    }
}
