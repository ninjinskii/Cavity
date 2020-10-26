package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "bottle")
data class Bottle(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "bottle_id")
    val bottleId: Long = 0,
    @ColumnInfo(name = "wine_id") val wineId: Long,
    val vintage: Int,
    val apogee: Int,
    @ColumnInfo(name = "is_favorite") var isFavorite: Int,
    val count: Int,
    val price: Int,
    val currency: String,
    @ColumnInfo(name = "other_info") var otherInfo: String,
    @ColumnInfo(name = "buy_location") val buyLocation: String,
    @ColumnInfo(name = "buy_date") val buyDate: Long,
    @ColumnInfo(name = "taste_comment") val tasteComment: String,
    @ColumnInfo(name = "pdf_path") var pdfPath: String
) {
    fun isReadyToDrink(): Boolean {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        return year >= apogee
    }
}

// Préparer un set de couleur clair avec légende pour identifier les cépages dans une jauge dans les infos de bouteille
// Context getFilesDIr() pour récupérer le stockage privé et mettre les photos dedans
