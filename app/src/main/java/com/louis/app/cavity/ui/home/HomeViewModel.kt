package com.louis.app.cavity.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.util.Event
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    fun deleteWine(wineId: Long) = viewModelScope.launch(IO) { repository.deleteWineById(wineId) }

    fun getAllCounties() = repository.getAllCounties()

    fun getWinesWithBottlesByCounty(countyId: Long) =
        repository.getWineWithBottlesByCounty(countyId).map {
            it.sortedBy { wineWithBottles -> wineWithBottles.wine.color.order }
        }
}
