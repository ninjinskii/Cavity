package com.louis.app.cavity.ui.bottle

import android.app.Application
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
import com.louis.app.cavity.util.minusAssign
import com.louis.app.cavity.util.plusAssign
import com.louis.app.cavity.util.postOnce
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

    private val _expertAdvices = MutableLiveData<MutableList<ExpertAdvice>>()
    val expertAdvices: LiveData<MutableList<ExpertAdvice>>
        get() = _expertAdvices

    fun addGrape(grape: Grape) {
        if (!alreadyContainsGrape(grape.name)) {
            viewModelScope.launch (IO) {
                repository.insertGrape(grape)
            }
        } else {
            _userFeedback.postOnce(R.string.grape_already_exist)
        }
    }

    fun updateGrape(grape: Grape) {
        viewModelScope.launch (IO) {
            repository.updateGrape(grape)
        }
    }

    fun removeGrape(grape: Grape) {
        viewModelScope.launch (IO) {
            repository.deleteGrape(grape)
        }
    }

    fun getAllGrapes() = repository.getAllGrapes()

    private fun alreadyContainsGrape(grapeName: String): Boolean {
        return grapeName in getAllGrapes().value?.map { it.name } ?: return false
    }

    fun addExpertAdvice(advice: ExpertAdvice) {
        advice.idBottle = bottleId ?: return
        _expertAdvices += advice
    }

    fun removeExpertAdvice(advice: ExpertAdvice) {
        _expertAdvices -= advice
    }

    fun addBottle(
        vintage: Int,
        apogee: Int,
        count: Int,
        price: Int,
        currency: String,
        location: String,
        date: String
    ) {
        val bottle = Bottle(
            idBottle = bottleId ?: 0,
            idWine = 0,
            vintage,
            apogee,
            isFavorite = 0,
            count,
            comment = "",
            price,
            currency,
            otherInfo = "",
            location,
            date,
            tasteComment = "",
            pdfPath = ""
        )

        viewModelScope.launch (IO) {
            bottleId = repository.insertBottle(bottle)
        }
    }

    // One bottle is added when user has complete step 1, then we get the id given by room to their bottle
    // and use it to add grapes, expert advices and other into later steps
    // If the user cancel the bottle form completion half-way, we should delete the bottle
    fun removeNotCompletedBottle() {
        bottleId?.let {
            viewModelScope.launch (IO) {
                repository.deleteBottleById(it)
                bottleId = null
            }
        }
    }
}
