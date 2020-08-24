package com.louis.app.cavity.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.CavityDatabase
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Wine
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository(
        CavityDatabase.getInstance(app).wineDao(),
        CavityDatabase.getInstance(app).bottleDao()
    )

    fun addWine(wine: Wine) = viewModelScope.launch (IO) {
        repository.insertWine(wine)
    }

    fun getAllWines() = repository.getAllWines()

    fun getAllCounties() = repository.getAllCounties()

    fun getWinesWithBottles() = repository.getWineWithBottles()
}
