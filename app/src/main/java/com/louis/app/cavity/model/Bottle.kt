package com.louis.app.cavity.model

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity
data class Bottle(
    @ColumnInfo(name = "id_wine") val idWine: Long,
    val apogee: Int,
    @ColumnInfo(name = "is_favorite") val isFavorite: Int,
    @ColumnInfo(name = "is_bio") val isBio: Int,
    val count: Int,
    val vintage: Int,
    val comment: String,
    val price: Int,
    val currency: String,
    @ColumnInfo(name = "buy_location") val buyLocation: String,
    @ColumnInfo(name = "buy_date") val buyDate: String,
    val contest: String,
    @ColumnInfo(name = "contest_comment") val contestComment: String,
    @ColumnInfo(name = "pdf_path") val pdfPath: String,
    @ColumnInfo(name = "img_path") val imgPath: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_bottle")
    var idBottle: Long = 0
}
// Plusieurs "avis d'expert" (ancienne distinction)
// Cépages
// Distinction (VT...)
// Autre
// Bio -> Vin

// RV pour sélection du cépage dans la bouteille, pour chaque cépage choisi, ajouter un nouveau slider vertical au rv
// Préparer un set de couleur clair avec légende pour identifier les cépages dans une jauge dans les infos de bouteille