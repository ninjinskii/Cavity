package com.louis.app.cavity.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toBoolean
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private val observedCounty = MutableLiveData<Long>()

    val bottleCount = observedCounty.switchMap {
        repository.getBottleCountForCounty(it)
    }

    val namingCount = observedCounty.switchMap {
        repository.getNamingsStatsForCounty(it)
    }

    fun setObservedCounty(countyId: Long) {
        observedCounty.value = countyId
    }

    fun deleteWine(wineId: Long) = viewModelScope.launch(IO) {
        repository.deleteWineById(wineId)
        _userFeedback.postOnce(R.string.wine_deleted)
    }

    fun getAllCounties() = repository.getAllCounties()

    // This become unecessary if we figure out how to implement Room's multimaps with standard SQL Join request
    fun getWinesWithBottlesByCounty(countyId: Long) = liveData(Default) {
        emitSource(
            repository.getWineWithBottlesByCounty(countyId).map { winesWithBottles ->
                winesWithBottles
                    //.filter { !it.hidden.toBoolean() }
                    .sortedBy { it.wine.color.order }
                    .map { wineWithBottles ->
                        wineWithBottles.copy(
                            bottles = wineWithBottles.bottles
                                .filter { !it.consumed.toBoolean() }
                                .sortedBy { it.vintage }
                        )
                    }
            }
        )
    }
}
