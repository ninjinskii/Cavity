package com.louis.app.cavity.model

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity(tableName = "bottle")
data class Bottle(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_bottle")
    val idBottle: Long = 0,
    @ColumnInfo(name = "id_wine") val idWine: Long,
    val vintage: Int,
    val apogee: Int,
    @ColumnInfo(name = "is_favorite") var isFavorite: Int,
    val count: Int,
    val price: Int,
    val currency: String,
    @ColumnInfo(name = "other_info") var otherInfo: String,
    @ColumnInfo(name = "buy_location") val buyLocation: String,
    @ColumnInfo(name = "buy_date") val buyDate: String,
    @ColumnInfo(name = "taste_comment") val tasteComment: String,
    @ColumnInfo(name = "pdf_path") var pdfPath: String
)

// RV pour sélection du cépage dans la bouteille, pour chaque cépage choisi, ajouter un nouveau slider vertical au rv
// Préparer un set de couleur clair avec légende pour identifier les cépages dans une jauge dans les infos de bouteille
// Context getFilesDIr() pour récupérer le stockage privé et mettre les photos dedans
