package com.louis.app.cavity.ui.addbottle.viewmodel

import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import kotlinx.coroutines.CoroutineScope

class DateManager(
    private val viewModelScope: CoroutineScope,
    private val repository: WineRepository,
    private val editedBottle: Bottle?,
    postFeedback: (Int) -> Unit
) {
    private var buyDateTimestamp = System.currentTimeMillis()

    var partialBottle: Step1Bottle? = null
        private set

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

        partialBottle = Step1Bottle(
            editedBottle?.id ?: 0,
            vintage,
            apogee,
            count,
            price,
            currency,
            location,
            buyDateTimestamp
        )
    }

    data class Step1Bottle(
        val id: Long,
        val vintage: Int,
        val apogee: Int,
        val count: Int,
        val price: Float,
        val currency: String,
        val location: String,
        val buyDate: Long
    )
}
