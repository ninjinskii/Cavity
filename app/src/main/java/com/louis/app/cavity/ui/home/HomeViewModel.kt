package com.louis.app.cavity.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.util.Event
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    val isScrollingToTop = MutableLiveData<Boolean>()

    fun deleteWine(wineId: Long) = viewModelScope.launch(IO) { repository.deleteWineById(wineId) }

    fun getAllCounties() = repository.getAllCounties()

    fun getWinesWithBottlesByCounty(countyId: Long) =
        repository.getWineWithBottlesByCounty(countyId)
}
