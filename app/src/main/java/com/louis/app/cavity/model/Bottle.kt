package com.louis.app.cavity.model

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity
data class Bottle(
    @ColumnInfo(name = "id_wine") var idWine: Long,
    var apogee: Int,
    @ColumnInfo(name = "is_favorite") var isFavorite: Int,
    @ColumnInfo(name = "is_bio") var isBio: Int,
    var count: Int,
    var vintage: Int,
    var comment: String,
    var price: Int,
    var currency: String,
    @ColumnInfo(name = "buy_location") var buyLocation: String,
    @ColumnInfo(name = "buy_date") var buyDate: String,
    var distinction: String,
    @ColumnInfo(name = "distinction_comment") var distinctionComment: String,
    @ColumnInfo(name = "pdf_path") var pdfPath: String,
    @ColumnInfo(name = "img_path") var imgPath: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_bottle")
    var idBottle: Long = 0
}