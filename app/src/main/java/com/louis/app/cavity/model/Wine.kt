package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.louis.app.cavity.ui.home.WineColor

@Entity(
    tableName = "wine",
    foreignKeys = [ForeignKey(
        entity = County::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("county_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Wine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val naming: String,
    val color: Int,
    val cuvee: String,
    @ColumnInfo(name = "county_id", index = true) val countyId: Long,
    @ColumnInfo(name = "is_organic") val isOrganic: Int,
    @ColumnInfo(name = "img_path") val imgPath: String
) {
    companion object {
        fun wineColorToColorNumber(color: WineColor): Int {
            return when (color) {
                WineColor.COLOR_WHITE -> 0
                WineColor.COLOR_RED -> 1
                WineColor.COLOR_SWEET -> 2
                else -> 3
            }
        }
    }
}
