package com.louis.app.cavity.ui.bottle

import android.app.Application
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.CavityDatabase
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.ExpertAdvice
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toBoolean
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AddBottleViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository(
        CavityDatabase.getInstance(app).wineDao(),
        CavityDatabase.getInstance(app).bottleDao()
    )

    private var bottleId: Long? = null

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    fun addGrape(grape: Grape) {
        if (!alreadyContainsGrape(grape.name)) {
            viewModelScope.launch(IO) {
                repository.insertGrape(grape)
            }
        } else {
            _userFeedback.postOnce(R.string.grape_already_exist)
        }
    }

    fun updateGrape(grape: Grape) {
        viewModelScope.launch(IO) {
            repository.updateGrape(grape)
        }
    }

    fun removeGrape(grape: Grape) {
        viewModelScope.launch(IO) {
            repository.deleteGrape(grape)
        }
    }

    fun getAllGrapes() = repository.getAllGrapes()

    private fun alreadyContainsGrape(grapeName: String): Boolean {
        return grapeName in getAllGrapes().value?.map { it.name } ?: return false
    }

    fun addExpertAdvice(advice: ExpertAdvice) {
        when {
            advice.isRate100.toBoolean() && advice.value !in 0..100 ->
                _userFeedback.postOnce(R.string.rate_outside_0_to_100)

            advice.isRate20.toBoolean() && advice.value !in 0..20 ->
                _userFeedback.postOnce(R.string.rate_outside_0_to_20)

            else -> {
                viewModelScope.launch(IO) {
                    repository.insertAdvice(advice)
                }
            }
        }
    }

    fun removeExpertAdvice(advice: ExpertAdvice) {
        viewModelScope.launch(IO) {
            repository.deleteAdvice(advice)
        }
    }

    fun getAllExpertAdvices() = repository.getAllExpertAdvices()

    fun addBottle(
        vintage: Int,
        apogee: Int,
        count: String,
        price: String,
        currency: String,
        location: String,
        date: String
    ): Boolean {
        var formattedPrice = price

        if (count.isEmpty() || !count.isDigitsOnly() || count.toInt() <= 0) {
            _userFeedback.postOnce(R.string.zero_bottle)
            return false
        }

        if (formattedPrice.isEmpty()) formattedPrice = "0"

        if (!price.isDigitsOnly()) {
            _userFeedback.postOnce(R.string.incorrect_price_format)
            return false
        }

        val bottle = Bottle(
            idBottle = bottleId ?: 0,
            idWine = 0,
            vintage,
            apogee,
            isFavorite = 0,
            count = count.toInt(),
            comment = "",
            price = formattedPrice.toInt(),
            currency,
            otherInfo = "",
            location,
            date,
            tasteComment = "",
            pdfPath = ""
        )

        viewModelScope.launch(IO) {
            bottleId = repository.insertBottle(bottle)
        }

        return true
    }

    // One bottle is added when user has complete step 1, then we get the id given by room to their bottle
    // and use it to add grapes, expert advices and other into later steps
    // If the user cancel the bottle form completion half-way, we should delete the bottle
    fun removeNotCompletedBottle() {
        bottleId?.let {
            viewModelScope.launch(IO) {
                repository.deleteBottleById(it)
                bottleId = null
            }
        }
    }
}
