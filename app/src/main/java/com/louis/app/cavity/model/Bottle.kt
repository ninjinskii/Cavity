package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "bottle",
    foreignKeys = [ForeignKey(
        entity = Wine::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("wine_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Bottle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "wine_id") val wineId: Long,
    val vintage: Int,
    val apogee: Int,
    @ColumnInfo(name = "is_favorite") var isFavorite: Int,
    val count: Int,
    val price: Float,
    val currency: String,
    @ColumnInfo(name = "other_info") var otherInfo: String,
    @ColumnInfo(name = "buy_location") val buyLocation: String,
    @ColumnInfo(name = "buy_date") val buyDate: Long,
    @ColumnInfo(name = "taste_comment") val tasteComment: String,
    @ColumnInfo(name = "pdf_path") var pdfPath: String,
    var consumed: Int
) {
    fun isReadyToDrink(): Boolean {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        return year >= apogee
    }

    fun hasPdf() = pdfPath.isNotBlank()
}
