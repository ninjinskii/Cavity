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
    private val repository = WineRepository(
        CavityDatabase.getInstance(app).wineDao(),
        CavityDatabase.getInstance(app).bottleDao()
    )

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback : LiveData<Event<Int>>
        get() = _userFeedback

    fun addWine(wine: Wine) = viewModelScope.launch(IO) {
        repository.insertWine(wine)
    }

    fun addCounty(countyName: String) {
        viewModelScope.launch(IO) {
            if (countyName.isNotEmpty()) {
                val counties = repository.getAllCountiesNotLive().map { it.name }

                if (!counties.contains(countyName)) {
                    repository.insertCounty(County(countyName, counties.size))
                } else {
                    _userFeedback.postOnce(R.string.county_already_exist)
                }
            } else {
                _userFeedback.postOnce(R.string.empty_county_name)
            }
        }
    }

    fun getAllWines() = repository.getAllWines()

    fun getAllCounties() = repository.getAllCounties()

    fun getWinesWithBottles() = repository.getWineWithBottles()
}
