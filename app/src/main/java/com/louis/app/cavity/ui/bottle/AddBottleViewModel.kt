package com.louis.app.cavity.ui.bottle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.util.minusAssign
import com.louis.app.cavity.util.plusAssign

class AddBottleViewModel(app: Application) : AndroidViewModel(app) {

    private val _grapes = MutableLiveData<MutableList<Grape>>()
    val grapes: LiveData<MutableList<Grape>>
        get() = _grapes

    fun addGrape(grape: Grape) {
        _grapes += grape
    }

    fun removeGrape(grape: Grape) {
        _grapes -= grape
    }
}