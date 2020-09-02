package com.louis.app.cavity.ui.bottle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.model.Grape

class AddBottleViewModel(app: Application) : AndroidViewModel(app) {
    private val _grapes = MutableLiveData<List<Grape>>()
    val grapes: LiveData<List<Grape>>
        get() = _grapes

    fun addGrapes() {

    }
}