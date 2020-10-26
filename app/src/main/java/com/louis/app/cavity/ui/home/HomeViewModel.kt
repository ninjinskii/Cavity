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

    fun deleteWine(wine: Wine) = viewModelScope.launch (IO) { repository.deleteWine(wine) }

    fun getAllCounties() = repository.getAllCounties()

    fun getWinesWithBottlesByCounty(countyId: Long) = repository.getWineWithBottlesByCounty(countyId)
}
