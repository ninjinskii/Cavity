package com.louis.app.cavity.ui.home

import android.app.Application
import android.util.EventLog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.CavityDatabase
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback : LiveData<Event<Int>>
        get() = _userFeedback

    val isScrollingToTop = MutableLiveData<Boolean>()
    var editWine : Wine? = null

    fun addWine(wine: Wine) = viewModelScope.launch(IO) {
        repository.insertWine(wine)
    }

    fun addCounty(countyName: String) {
        viewModelScope.launch(IO) {
            if (countyName.isNotEmpty()) {
                val counties = repository.getAllCountiesNotLive().map { it.name }

                if (countyName !in counties) {
                    repository.insertCounty(County(name = countyName, prefOrder = counties.size))
                } else {
                    _userFeedback.postOnce(R.string.county_already_exist)
                }
            } else {
                _userFeedback.postOnce(R.string.empty_county_name)
            }
        }
    }

    fun computeBottleCountByCounty() {
//        val counties = repository.getAllCountiesNotLive()
//
//        for (county in counties) {
//            val winesWithBottles = repository.getWineWithBottlesByCounty(county.countyId)
//            var count = 0
//
//            winesWithBottles.value?.forEach { count += it.bottles.size }
//        }
    }

    fun getAllWines() = repository.getAllWines()

    fun updateWine(wine: Wine) = viewModelScope.launch (IO) { repository.updateWine(wine) }

    fun deleteWine(wine: Wine) = viewModelScope.launch (IO) { repository.deleteWine(wine) }

    fun getAllBottles() = repository.getAllBottles()

    fun getAllCounties() = repository.getAllCounties()

    fun getWinesWithBottles() = repository.getWineWithBottles()

    fun getWinesWithBottlesByCounty(countyId: Long) = repository.getWineWithBottlesByCounty(countyId)
}
