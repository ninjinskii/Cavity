package com.louis.app.cavity.model

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity(tableName = "bottle")
data class Bottle(
    @ColumnInfo(name = "id_wine") val idWine: Long,
    val apogee: Int,
    @ColumnInfo(name = "is_favorite") val isFavorite: Int,
    val count: Int,
    val vintage: Int,
    val comment: String,
    val price: Int,
    val currency: String,
    val distinction: String,
    @ColumnInfo(name = "other_info") val otherInfo: String,
    @ColumnInfo(name = "buy_location") val buyLocation: String,
    @ColumnInfo(name = "buy_date") val buyDate: String,
    @ColumnInfo(name = "contest_comment") val contestComment: String,
    @ColumnInfo(name = "pdf_path") val pdfPath: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_bottle")
    var idBottle: Long = 0
}

// RV pour sélection du cépage dans la bouteille, pour chaque cépage choisi, ajouter un nouveau slider vertical au rv
// Préparer un set de couleur clair avec légende pour identifier les cépages dans une jauge dans les infos de bouteille