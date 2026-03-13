package com.louis.app.cavity.ui.home

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.louis.app.cavity.db.dao.WineWithBottles
import com.louis.app.cavity.domain.error.SentryErrorReporter
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.CountyRepository
import com.louis.app.cavity.domain.repository.PrefsRepository
import com.louis.app.cavity.domain.repository.StatsRepository
import com.louis.app.cavity.domain.repository.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.County
import com.louis.app.cavity.ui.BaseViewModel
import com.louis.app.cavity.ui.navigation.AppRoute
import com.louis.app.cavity.ui.navigation.HomeRoute
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.access
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

sealed interface HomeEvent {
    data class Navigation(val appRoute: AppRoute) : HomeEvent
    object WinesObservingStarted : HomeEvent
}

data class LastWineChange(val wineId: Long, val countyId: Long)
data class HomeState(val lastWineChange: LastWineChange? = null)

// TODO: change consumers lifecycle scope to fragment instead of activity. It will break
// storage locaton filter feature, as it relies on navigating from home to home to update filter
// If this viewmodel is scoped to home fragment, it will be recreated, thus loosign the storage location value
// This should be refactored when using only flows, for now, keep as it is so that we can focus on navigation only
class HomeViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle
) :
    BaseViewModel<HomeState, HomeEvent>(app, HomeState()) {

    private val countyRepository = CountyRepository.getInstance(app)
    private val wineRepository = WineRepository.getInstance(app)
    private val bottleRepository = BottleRepository.getInstance(app)
    private val statsRepository = StatsRepository.getInstance(app)
    private val prefsRepository = PrefsRepository.getInstance(app)
    private val errorReporter = SentryErrorReporter.getInstance(app)

    /**
     * countyId from the arguments of the fragment that initiated a navigation shared element
     * transition to FragmentBottleDetails. Used to remember what fragment of the ViewPager we
     * should focus to resume postponed FragmentWinesParent transition
     */
    var savedSharedElementCountyId: Long? by savedStateHandle access "sourceCountyId"

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private val _storageLocation = MutableStateFlow<String?>(null)
    val storageLocation: StateFlow<String?> = _storageLocation.asStateFlow()

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

    @OptIn(ExperimentalCoroutinesApi::class)
    val nonEmptyCounties: LiveData<List<County>> = _storageLocation.flatMapLatest { location ->
        if (prefsRepository.getEnableBottleStorageLocation() && location != null)
            countyRepository.getNonEmptyCountiesForStorageLocation(location).asFlow()
        else
            countyRepository.getNonEmptyCounties().asFlow()
    }.asLiveData()

    fun notifyWineChange(wineId: Long, countyId: Long) {
        stateFlow.update { state ->
            state.copy(lastWineChange = LastWineChange(wineId, countyId))
        }
    }

    fun acknowledgeWineChange() {
        stateFlow.update { state ->
            state.copy(lastWineChange = null)
        }
    }

    fun setObservedCounty(countyId: Long) {
        observedCounty.value = countyId
    }

    fun setStorageLocation(bottleStorage: String?, currentCountyId: Long?) {
//        savedStorageLocation = bottleStorage
        _storageLocation.value = bottleStorage
        countyIdBeforeStorageLocationChange = currentCountyId
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

    fun getAllStorageLocations(clearText: String) =
        if (prefsRepository.getEnableBottleStorageLocation())
            bottleRepository.getAllStorageLocations().map { listOf(clearText) + it }
        else MutableLiveData(emptyList())

    fun getWinesWithBottlesByCounty(countyId: Long) = liveData(Default) {
        if (countyId < 1) {
            errorReporter.captureMessage("Illegal county id: $countyId")
        }

        val wines = wineRepository.getWinesWithBottlesByCounty(countyId).map { winesWithBottles ->
            winesWithBottles.filter { checkStorageLocation(it) }
        }

        emitSource(wines)
    }

    fun notifyWineObservingStarted(countyId: Long) {
        val noPendingSharedElement = savedSharedElementCountyId == null
        val isAssociatedCounty = savedSharedElementCountyId == countyId

        if (isAssociatedCounty || noPendingSharedElement) {
            emitEvent(HomeEvent.WinesObservingStarted)
        }
    }

    fun handleWineClick(wineWithBottles: WineWithBottles, requesterFragmentCountyId: Long) {
        checkCounty(wineWithBottles, requesterFragmentCountyId)
        savedSharedElementCountyId = requesterFragmentCountyId

        val wineId = wineWithBottles.wine.id
        val route = when {
            isEmptyWine(wineWithBottles) -> HomeRoute.AddBottle(wineId)
            else -> HomeRoute.BottleDetails(wineId)
        }

        emitEvent(HomeEvent.Navigation(route))
    }

    fun handleWineLongClick(wineWithBottles: WineWithBottles, fragmentCountyId: Long) {
        checkCounty(wineWithBottles, fragmentCountyId)
        val storageLocationActive = _storageLocation.value != null
        val route = HomeRoute.WineOptions(wineWithBottles.wine, storageLocationActive)
        emitEvent(HomeEvent.Navigation(route))
    }

    private fun checkCounty(wineWithBottles: WineWithBottles, fragmentCountyId: Long) {
        if (wineWithBottles.wine.countyId != fragmentCountyId) {
            throw IllegalStateException("Wine view holder listener has wrong FragmentWines context")
        }
    }

    private fun isEmptyWine(wineWithBottles: WineWithBottles): Boolean {
        val (_, bottles, remainingBottles) = wineWithBottles
        val nonEmpty = remainingBottles != bottles.size || bottles.isNotEmpty()
        return !nonEmpty
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

        return wineWithBottles.bottles.any {
            checkStorageLocation(it) && wineWithBottles.remainingBottles > 0
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val savedState = createSavedStateHandle()
                HomeViewModel(app, savedState)
            }
        }
    }
}
