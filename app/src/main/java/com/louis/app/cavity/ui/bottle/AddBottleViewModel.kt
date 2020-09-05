package com.louis.app.cavity.ui.bottle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.model.ExpertAdvice
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.util.minusAssign
import com.louis.app.cavity.util.plusAssign

class AddBottleViewModel(app: Application) : AndroidViewModel(app) {

    private val _grapes = MutableLiveData<MutableList<Grape>>()
    val grapes: LiveData<MutableList<Grape>>
        get() = _grapes

    private val _expertAdvices = MutableLiveData<MutableList<ExpertAdvice>>()
    val expertAdvices: LiveData<MutableList<ExpertAdvice>>
        get() = _expertAdvices

    fun addGrape(grape: Grape) {
        _grapes += grape
    }

    fun removeGrape(grape: Grape) {
        _grapes -= grape
    }

    fun alreadyContainsGrape(grapeName: String): Boolean {
        return grapeName in _grapes.value?.map { it.name } ?: return false
    }

    fun addExpertAdvice(advice: ExpertAdvice) {
        _expertAdvices += advice
    }

    fun removeExpertAdvice(advice: ExpertAdvice) {
        _expertAdvices -= advice
    }
}