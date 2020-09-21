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
import com.louis.app.cavity.util.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AddBottleViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository(
        CavityDatabase.getInstance(app).wineDao(),
        CavityDatabase.getInstance(app).bottleDao()
    )

    private var bottleId: Long? = null
    var wineId: Long? = null

    private val _expertAdvices = MutableLiveData<MutableList<ExpertAdvice>>()
    val expertAdvices: LiveData<MutableList<ExpertAdvice>>
        get() = _expertAdvices

    private val _grapes = MutableLiveData<MutableList<Grape>>()
    val grapes: LiveData<MutableList<Grape>>
        get() = _grapes

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    fun addGrape(grapeName: String, defaultPercentage: Int) {
        bottleId?.let {
            val grape = Grape(0, grapeName, defaultPercentage, it)

            if (!alreadyContainsGrape(grape.name))
                _grapes += grape
            else
                _userFeedback.postOnce(R.string.grape_already_exist)
        }
    }

    fun updateGrape(grape: Grape) {
        grapes.value?.first { it.name == grape.name }?.percentage = grape.percentage
    }

    fun removeGrape(grape: Grape) {
        _grapes -= grape
    }

    private fun alreadyContainsGrape(grapeName: String): Boolean {
        val grapeNames = _grapes.value?.map { it.name } ?: return false
        return grapeName in grapeNames
    }

    fun addExpertAdvice(contestName: String, typeToVal: Pair<AdviceType, Int>) {
        if (contestName.isEmpty()) {
            _userFeedback.postOnce(R.string.empty_contest_name)
            return
        }

        if (alreadyContainsAdvice(contestName)) {
            _userFeedback.postOnce(R.string.contest_name_already_exist)
            return
        }

        bottleId?.let {
            val advice: ExpertAdvice? = when (typeToVal.first) {
                AdviceType.RATE_20 -> {
                    if (checkRateInBounds(typeToVal.second, 20))
                        ExpertAdvice(0, contestName, 0, 0, 1, 0, typeToVal.second, it)
                    else
                        null
                }
                AdviceType.RATE_100 -> {
                    if (checkRateInBounds(typeToVal.second, 100))
                        ExpertAdvice(0, contestName, 0, 0, 0, 1, typeToVal.second, it)
                    else
                        null
                }
                AdviceType.MEDAL -> ExpertAdvice(0, contestName, 1, 0, 0, 0, typeToVal.second, it)
                else -> ExpertAdvice(0, contestName, 0, 1, 0, 0, typeToVal.second, it)
            }

            advice?.let { adv -> _expertAdvices += adv }
        }
    }

    private fun checkRateInBounds(rate: Int, max: Int): Boolean {
        return if (rate in 0..max) {
            true
        } else {
            _userFeedback.postOnce(R.string.rate_out_of_bounds)
            false
        }
    }

    fun removeExpertAdvice(advice: ExpertAdvice) {
        _expertAdvices -= advice
    }

    private fun alreadyContainsAdvice(contestName: String): Boolean {
        val advicesName = _expertAdvices.value?.map { it.contestName } ?: return false
        return contestName in advicesName
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

    // Add partial bottle with base informations, and retrieve the id given by Room, which will be used to associate grapes and expert advices
    fun addPartialBottle(
        vintage: Int,
        apogee: Int,
        count: String,
        price: String,
        currency: String,
        location: String,
        date: String
    ) {
        val formattedPrice = if (price.isEmpty()) "-1" else price
        val bottle = Bottle(
            idBottle = bottleId ?: 0,
            idWine = wineId!!,
            vintage,
            apogee,
            isFavorite = 0,
            count = count.toInt(),
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
    }

    fun triggerFinalBottleSave(otherInfo: String, addToFavorite: Boolean, pdfPath: String) {
        if (bottleId != null) {
            viewModelScope.launch(IO) {
                val bottle = repository.getBottleByIdNotLive(bottleId!!)
                bottle.apply {
                    this.otherInfo = otherInfo
                    isFavorite = addToFavorite.toInt()
                    this.pdfPath = pdfPath
                }

                repository.updateBottle(bottle)

                _expertAdvices.value?.forEach {
                    it.idBottle = bottleId!!
                    repository.insertAdvice(it)
                }

                _grapes.value?.forEach {
                    it.idBottle = bottleId!!
                    repository.insertGrape(it)
                }
            }
        } else {
            _userFeedback.postOnce(R.string.base_error)
            return
        }
    }

    // Called when the user exits add bottle process before validating the process
    fun removeNotCompletedBottle() {
        viewModelScope.launch(IO) {
            repository.deleteBottleById(bottleId ?: -1)
            bottleId = null
            wineId = null
        }
    }
}
