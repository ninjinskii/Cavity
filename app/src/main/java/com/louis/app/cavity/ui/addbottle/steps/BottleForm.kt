package com.louis.app.cavity.ui.addbottle.steps

import androidx.annotation.StringRes
import com.louis.app.cavity.R
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.ExpertAdvice
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.util.toInt

class BottleForm(private val wineId: Long) {
    private data class DatesForm(
        val vintage: Int,
        val apogee: Int,
        val count: Int,
        val price: Int,
        val currency: String,
        val buyLoacation: String,
        val buyDate: Long
    )

    private data class GrapesForm(val grapes: List<Grape>)
    private data class ExpertAdvicesForm(val expertAdvices: List<ExpertAdvice>)
    private data class OtherForm(
        val otherInfo: String,
        val addToFavorite: Boolean,
        val pdfPath: String
    )

    data class Validation(val success: Boolean, @StringRes val errorResource: Int)

    private val datesForm: DatesForm? = null
    private val grapesForm: GrapesForm? = null
    private val expertAdvicesForm: ExpertAdvicesForm? = null
    private val otherForm: OtherForm? = null

    fun getBottle(): Bottle {
        listOf(datesForm, grapesForm, expertAdvicesForm, otherForm).forEach { checkNotNull(it) }
        val (vintage, apogee, count, price, currency, buyLocation, buyDate) = datesForm!!
        val (otherInfo, addToFavorite, pdfPath) = otherForm!!

        return Bottle(
            -1,
            wineId,
            vintage,
            apogee,
            addToFavorite.toInt(),
            count,
            price,
            currency,
            otherInfo,
            buyLocation,
            buyDate,
            "",
            pdfPath
        )
    }

    fun submitDatesForm(
        vintage: Int,
        apogee: Int,
        count: String,
        price: String,
        currency: String,
        buyLocation: String,
        buyDate: Long
    ): Validation {
        var formattedPrice = -1F
        var formattedCount = 1
        var message = R.string.falsy_price

        try {
            formattedPrice = if (price.isEmpty()) -1F else price.toFloat()
            message = R.string.falsy_count
            formattedCount = count.toInt()
        } catch (e: NumberFormatException) {
            return Validation(success = false, errorResource = message)
        }

        return Validation(success = true, errorResource = -1)
    }
}
