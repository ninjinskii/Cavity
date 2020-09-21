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
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AddBottleViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository(
        CavityDatabase.getInstance(app).wineDao(),
        CavityDatabase.getInstance(app).bottleDao()
    )

    val expertAdvices = mutableListOf<ExpertAdvice>()
    val grapes = mutableListOf<Grape>()

    private var step1Bottle: Step1Bottle? = null
    var wineId: Long? = null

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    fun addGrape(grape: Grape) {
        if (!alreadyContainsGrape(grape.name))
            grapes.add(grape)
        else
            _userFeedback.postOnce(R.string.grape_already_exist)
    }

    fun updateGrape(grape: Grape) {
        grapes.first { it.name == grape.name }.percentage = grape.percentage
    }

    fun removeGrape(grape: Grape) = grapes.removeAt(grapes.indexOf(grape))

    private fun alreadyContainsGrape(grapeName: String) = grapeName in grapes.map { it.name }

    fun addExpertAdvice(advice: ExpertAdvice) {
        when {
            advice.isRate100.toBoolean() && advice.value !in 0..100 ->
                _userFeedback.postOnce(R.string.rate_outside_0_to_100)

            advice.isRate20.toBoolean() && advice.value !in 0..20 ->
                _userFeedback.postOnce(R.string.rate_outside_0_to_20)

            else ->
                expertAdvices.add(advice)
        }
    }

    fun removeExpertAdvice(advice: ExpertAdvice) {
        expertAdvices.removeAt(expertAdvices.indexOf(advice))
    }

    fun validateBottle(count: String, price: String): Boolean {
        if (count.isEmpty() || !count.isDigitsOnly() || count.toInt() <= 0) {
            _userFeedback.postOnce(R.string.zero_bottle)
            return false
        }

        if (!price.isDigitsOnly()) {
            _userFeedback.postOnce(R.string.incorrect_price_format)
            return false
        }

        return true
    }

    fun saveStep1Bottle(partialBottle: Step1Bottle) {
        step1Bottle = partialBottle
    }

    fun addBottle(otherInfo: String, isFavorite: Boolean, pdfPath: String) {
        if (step1Bottle != null) {
            with(step1Bottle ?: return) {
                val formattedPrice = if (price.isEmpty()) "-1" else price
                val bottle = Bottle(
                    idBottle = 0,
                    idWine = wineId!!,
                    vintage,
                    apogee,
                    isFavorite = isFavorite.toInt(),
                    count = count.toInt(),
                    price = formattedPrice.toInt(),
                    currency,
                    otherInfo,
                    location,
                    date,
                    tasteComment = "",
                    pdfPath
                )

                viewModelScope.launch(IO) {
                    repository.insertBottle(bottle)
                }
            }
        } else {
            _userFeedback.postOnce(R.string.base_error)
            return
        }
    }

    data class Step1Bottle(
        val vintage: Int,
        val apogee: Int,
        val count: String,
        val price: String,
        val currency: String,
        val location: String,
        val date: String
    )
}
