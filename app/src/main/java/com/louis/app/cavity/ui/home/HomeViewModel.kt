package com.louis.app.cavity.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.db.CavityDatabase
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Wine

class HomeViewModel(app: Application) :AndroidViewModel(app) {
    private val repository = WineRepository(CavityDatabase.getInstance(app).wineDao())

//    private val _wines = MutableLiveData<List<Wine>>()
//    val wines: LiveData<List<Wine>>
//        get() = _wines

    fun getAllWines() = repository.getAllWines()
}