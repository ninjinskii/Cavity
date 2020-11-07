package com.louis.app.cavity.ui.addbottle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.ExpertAdvice
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.util.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AddBottleViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _expertAdvices = MutableLiveData<MutableList<ExpertAdvice>>()
    val expertAdvices: LiveData<MutableList<ExpertAdvice>>
        get() = _expertAdvices

    private val _grapes = MutableLiveData<MutableList<Grape>>()
    val grapes: LiveData<MutableList<Grape>>
        get() = _grapes

    private val _updatedBottle = MutableLiveData<Bottle>()
    val updatedBottle: LiveData<Bottle>
        get() = _updatedBottle

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private var wineId: Long? = null
    private var partialBottle: PartialBottle? = null
    private var buyDateTimestamp = -1L
    private var pdfPath: String = ""
    private val isEditMode: Boolean
        get() = _updatedBottle.value != null
    val hasPdf: Boolean
        get() = pdfPath.isNotBlank()

    fun start(bottleWineId: Long, editedBottleId: Long) {
        wineId = bottleWineId

        if (editedBottleId != -1L)
            triggerEditMode(editedBottleId)
        else
            _updatedBottle.postValue(null)
    }

    fun addGrape(grapeName: String, defaultPercentage: Int) {
        val grape = Grape(0, grapeName, defaultPercentage, -1)

        if (!alreadyContainsGrape(grape.name))
            _grapes += grape
        else
            _userFeedback.postOnce(R.string.grape_already_exist)
    }

    fun updateGrape(grape: Grape) {
        grapes.value?.first { it.name == grape.name }?.percentage = grape.percentage
    }

    fun removeGrape(grape: Grape) {
        // Deleted grape might already be in database, we need to remove it
        if (isEditMode) viewModelScope.launch(IO) {
            repository.deleteGrape(grape)
        }

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

        val advice: ExpertAdvice? = when (typeToVal.first) {
            AdviceType.RATE_20 -> {
                if (checkRateInBounds(typeToVal.second, 20))
                    ExpertAdvice(0, contestName, 0, 0, 1, 0, typeToVal.second, -1)
                else
                    null
            }
            AdviceType.RATE_100 -> {
                if (checkRateInBounds(typeToVal.second, 100))
                    ExpertAdvice(0, contestName, 0, 0, 0, 1, typeToVal.second, -1)
                else
                    null
            }
            AdviceType.MEDAL -> ExpertAdvice(0, contestName, 1, 0, 0, 0, typeToVal.second, -1)
            else -> ExpertAdvice(0, contestName, 0, 1, 0, 0, typeToVal.second, -1)
        }

        advice?.let { adv -> _expertAdvices += adv }
    }

    fun removeExpertAdvice(advice: ExpertAdvice) {
        // Deleted expert advice might already be in database, need to remove it
        if (isEditMode) viewModelScope.launch(IO) {
            repository.deleteAdvice(advice)
        }

        _expertAdvices -= advice
    }


    fun setTimestamp(timestamp: Long) {
        buyDateTimestamp = timestamp
    }

    fun setPdfPath(path: String) {
        pdfPath = path
    }

    fun setPartialBottle(
        vintage: Int,
        apogee: Int,
        count: Int,
        price: Float,
        currency: String,
        location: String
    ) {
        val editBottleId = _updatedBottle.value?.bottleId

        partialBottle =
            PartialBottle(
                editBottleId ?: 0,
                vintage,
                apogee,
                count,
                price,
                currency,
                location,
                buyDateTimestamp
            )
    }

    fun saveBottle(otherInfo: String, addToFavorite: Boolean) {
        if (wineId == null) _userFeedback.postOnce(R.string.base_error)

        partialBottle?.let {
            val bottle = Bottle(
                it.bottleId,
                wineId!!,
                it.vintage,
                it.apogee,
                addToFavorite.toInt(),
                it.count,
                it.price,
                it.currency,
                otherInfo,
                it.location,
                it.date,
                "",
                pdfPath
            )

            viewModelScope.launch(IO) {
                val insertedBottleId =
                    if (isEditMode) {
                        repository.updateBottle(bottle)
                        bottle.bottleId
                    } else {
                        repository.insertBottle(bottle)
                    }

                _expertAdvices.value?.forEach { advice ->
                    advice.bottleId = insertedBottleId
                    repository.insertAdvice(advice)
                }

                _grapes.value?.forEach { grape ->
                    grape.bottleId = insertedBottleId
                    repository.insertGrape(grape)
                }

                wineId = null
                partialBottle = null
                _grapes.postValue(mutableListOf())
                _expertAdvices.postValue(mutableListOf())
                _updatedBottle.postValue(null)
            }
        }
    }

    private fun triggerEditMode(bottleId: Long) {
        viewModelScope.launch(IO) {
            val editedBottle = repository.getBottleByIdNotLive(bottleId)
//            with(editedBottle) {
//                setPartialBottle(
//                    vintage,
//                    apogee,
//                    count.toString(),
//                    price.toString(),
//                    currency,
//                    buyLocation,
//                    buyDate
//                )
//            }

            L.v("editedWineId: $wineId")
            L.v("editedBottle: $editedBottle")
            _updatedBottle.postValue(editedBottle)

            val grapesForBottle = repository.getGrapesForBottleNotLive(bottleId)
            _grapes.postValue(grapesForBottle as MutableList<Grape>)

            val expertAdviceForBottle = repository.getExpertAdvicesForBottleNotLive(bottleId)
            _expertAdvices.postValue(expertAdviceForBottle as MutableList<ExpertAdvice>)
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

    private fun alreadyContainsAdvice(contestName: String): Boolean {
        val advicesName = _expertAdvices.value?.map { it.contestName } ?: return false
        return contestName in advicesName
    }

    data class PartialBottle(
        val bottleId: Long,
        val vintage: Int,
        val apogee: Int,
        val count: Int,
        val price: Float,
        val currency: String,
        val location: String,
        val date: Long
    )
}
