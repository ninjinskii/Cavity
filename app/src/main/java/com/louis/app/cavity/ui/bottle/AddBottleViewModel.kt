package com.louis.app.cavity.ui.bottle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.minusAssign
import com.louis.app.cavity.util.plusAssign

class AddBottleViewModel(app: Application) : AndroidViewModel(app) {
    private val maxGrapeQty = 100
    private var totalGrapeQty = 0

    private val _grapes = MutableLiveData<MutableList<Grape>>()
    val grapes: LiveData<MutableList<Grape>>
        get() = _grapes

    //private var totalGrapeQty = _grapes.value?.map { it.percentage }?.sum() ?: 0

    fun addGrape(grape: Grape) {
        L.v("add grape")
        _grapes += grape
        computeTotalPercentage()
    }

    fun removeGrape(grape: Grape) {
        _grapes -= grape
    }

    fun computeTotalPercentage() {
        totalGrapeQty = _grapes.value?.map { it.percentage }?.sum() ?: 0
    }

    fun isNewValueAllowed(value: Int) = totalGrapeQty + value <= maxGrapeQty

    fun getMaxAvailable() = maxGrapeQty - totalGrapeQty
}