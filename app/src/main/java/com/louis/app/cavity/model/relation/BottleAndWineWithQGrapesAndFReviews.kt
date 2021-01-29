package com.louis.app.cavity.model.relation

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Wine
import java.util.*

data class BottleAndWineWithQGrapesAndFReviews(
    @ColumnInfo(name = "wine_id") val wineId: Long,
    @ColumnInfo(name = "bottle_id") val bottleId: Long,
    val name: String,
    val naming: String,
    val cuvee: String,
    val color: Int,
    @ColumnInfo(name = "is_organic") val isOrganic: Int,
    val vintage: Int,
    val apogee: Int,
    @ColumnInfo(name = "is_favorite") val isFavorite: Int,
    val count: Int,
    val price: Int,
    val currency: String,
    @ColumnInfo(name = "other_info") val otherInfo: String,
    @ColumnInfo(name = "buy_location") val buyLocation: String,
    @ColumnInfo(name = "buy_date") val buyDate: Long,
    @ColumnInfo(name = "taste_comment") val tasteComment: String,
    @ColumnInfo(name = "pdf_path") val pdfPath: String,
    @ColumnInfo(name = "county_id") val countyId: Long,
    @Relation(
        entity = QuantifiedBottleGrapeXRef::class,
        parentColumn = "bottle_id",
        entityColumn = "bottle_id",
    )
    val qGrapes: List<QuantifiedBottleGrapeXRef>,
    @Relation(
        entity = FilledBottleReviewXRef::class,
        parentColumn = "bottle_id",
        entityColumn = "bottle_id",
    )
    val fReviews: List<FilledBottleReviewXRef>
) {
    fun isReadyToDrink(): Boolean {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        return year >= apogee
    }
}
