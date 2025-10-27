package com.louis.app.cavity.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.R
import com.louis.app.cavity.db.dao.WineWithBottles
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.CountyRepository
import com.louis.app.cavity.domain.repository.PrefsRepository
import com.louis.app.cavity.domain.repository.StatsRepository
import com.louis.app.cavity.domain.repository.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toBoolean
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val countyRepository = CountyRepository.getInstance(app)
    private val wineRepository = WineRepository.getInstance(app)
    private val bottleRepository = BottleRepository.getInstance(app)
    private val statsRepository = StatsRepository.getInstance(app)
    private val prefsRepository = PrefsRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private val _storageLocation = MutableLiveData<String?>()
    val storageLocation: LiveData<String?>
        get() = _storageLocation

    private val _lastAddedWine = MutableLiveData<Event<Pair<Wine, County>>>()
    val lastAddedWine: LiveData<Event<Pair<Wine, County>>>
        get() = _lastAddedWine

    private val _scrollToCountyEvent = MutableLiveData<Event<Int>>()
    val scrollToCountyEvent: LiveData<Event<Int>>
        get() = _scrollToCountyEvent

    private val observedCounty = MutableLiveData<Long>()

    private var countyIdBeforeStorageLocationChange: Long? = null

    val bottleCount = observedCounty.switchMap {
        statsRepository.getBottleCountForCounty(it, _storageLocation.value)
    }

    val bottlePrice = observedCounty.switchMap {
        statsRepository.getPriceByCurrencyForCounty(it, _storageLocation.value)
    }

    val namingCount = observedCounty.switchMap {
        statsRepository.getNamingsStatsForCounty(it, _storageLocation.value)
    }

    val vintagesCount = observedCounty.switchMap {
        statsRepository.getVintagesStatsForCounty(it, _storageLocation.value)
    }

    fun setLastAddedWine(wine: Wine) {
        viewModelScope.launch(IO) {
            countyRepository.getCountyByIdNotLive(wine.countyId)?.let { county ->
                _lastAddedWine.postOnce(wine to county)
            }
        }
    }

    fun setObservedCounty(countyId: Long) {
        observedCounty.value = countyId
    }

    fun setStorageLocation(bottleStorage: String?, currentCountyId: Long?) {
        _storageLocation.value = bottleStorage
        countyIdBeforeStorageLocationChange = currentCountyId
    }

    fun notifyStorageLocation() {
        _storageLocation.value.let {
            _storageLocation.value = null
            _storageLocation.value = it
        }
    }

    fun checkRememberedCountyBeforeStorageChange(counties: List<County>) {
        counties
            .indexOfFirst { county ->
                county.id == countyIdBeforeStorageLocationChange
            }
            .let { index ->
                if (index >= 0) {
                    countyIdBeforeStorageLocationChange = null
                    _scrollToCountyEvent.postOnce(index)
                }
            }
    }

    fun deleteOrHideWine(wineId: Long) = viewModelScope.launch(IO) {
        val wineBottles = bottleRepository.getBottlesForWineNotLive(wineId)
        val folder = mutableListOf<Bottle>() to mutableListOf<Bottle>()
        val (consumed, stock) = wineBottles.fold(folder) { pair, bottle ->
            pair.apply {
                when (bottle.consumed.toBoolean()) {
                    true -> first += bottle
                    else -> second += bottle
                }
            }
        }

        bottleRepository.deleteBottles(stock)

        when {
            consumed.isNotEmpty() -> wineRepository.hideWineById(wineId)
            else -> wineRepository.deleteWineById(wineId)
        }

        // We dirty liers
        _userFeedback.postOnce(R.string.wine_deleted)
    }

    fun getNonEmptyCounties() =
        if (prefsRepository.getEnableBottleStorageLocation() && _storageLocation.value != null)
            countyRepository.getNonEmptyCountiesForStorageLocation(_storageLocation.value ?: "")
        else countyRepository.getNonEmptyCounties()

    fun getAllStorageLocations(clearText: String) =
        if (prefsRepository.getEnableBottleStorageLocation())
            bottleRepository.getAllStorageLocations().map { listOf(clearText) + it }
        else MutableLiveData(emptyList())

    // This become unnecessary if we figure out how to implement Room's multimaps with standard SQL Join request
    fun getWinesWithBottlesByCounty(countyId: Long) = liveData(Default) {
        emitSource(
            wineRepository.getWineWithBottlesByCounty(countyId).map { winesWithBottles ->
                winesWithBottles
                    .filter { checkStorageLocation(it) }
                    .sortedBy { it.wine.color.order }
                    .map { wineWithBottles ->
                        wineWithBottles.copy(
                            bottles = wineWithBottles.bottles
                                .filter { !it.consumed.toBoolean() && checkStorageLocation(it) }
                                .sortedBy { it.vintage }
                        )
                    }
            }
        )
    }

    private fun checkStorageLocation(bottle: Bottle): Boolean {
        if (_storageLocation.value == null) {
            return true
        }

        return bottle.storageLocation == storageLocation.value
    }

    private fun checkStorageLocation(wineWithBottles: WineWithBottles): Boolean {
        if (_storageLocation.value == null) {
            return true
        }

        return wineWithBottles.bottles.any { checkStorageLocation(it) }
    }
}
