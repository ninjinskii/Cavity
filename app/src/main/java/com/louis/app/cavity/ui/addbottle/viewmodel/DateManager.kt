package com.louis.app.cavity.ui.addbottle.viewmodel

import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.CoroutineScope

class DateManager(
    private val viewModelScope: CoroutineScope,
    private val repository: WineRepository,
    private val editedBottle: Bottle?,
    postFeedback: (Int) -> Unit
) {
    private var buyDateTimestamp = System.currentTimeMillis()
    var partialBottle: Bottle? = null

    init {
        editedBottle?.let {
            setBuyDate(it.buyDate)
        }
    }

    fun setBuyDate(timestamp: Long) {
        buyDateTimestamp = timestamp
    }

    fun submitDates(
        vintage: Int,
        apogee: Int,
        count: Int,
        price: Float,
        currency: String,
        location: String
    ) {
        // These are from other info, should be useless
        val otherInfo = editedBottle?.otherInfo.orEmpty()
        val isFavorite = editedBottle?.isFavorite ?: 0
        val pdfPath = editedBottle?.pdfPath.orEmpty()
        val tasteComment = editedBottle?.tasteComment.orEmpty()

        partialBottle = Bottle(
            editedBottle?.id ?: 0,
            0, // Will be replaced by AddBottleViewModel
            vintage,
            apogee,
            isFavorite,
            count,
            price,
            currency,
            otherInfo,
            location,
            buyDateTimestamp,
            tasteComment,
            pdfPath,
            consumed = false.toInt()
        )
    }
}
