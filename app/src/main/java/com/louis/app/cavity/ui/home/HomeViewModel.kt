package com.louis.app.cavity.ui.home

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.louis.app.cavity.db.dao.WineWithBottles
import com.louis.app.cavity.domain.delegates.GetCountyDetails
import com.louis.app.cavity.domain.error.ErrorReporter
import com.louis.app.cavity.domain.error.ErrorReporterFactory
import com.louis.app.cavity.domain.error.SentryErrorReporter
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.CountyRepository
import com.louis.app.cavity.domain.repository.PrefsRepository
import com.louis.app.cavity.domain.repository.WineRepository
import com.louis.app.cavity.domain.stats.RoomStatsQueries
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.CountyDetails
import com.louis.app.cavity.ui.BaseViewModel
import com.louis.app.cavity.ui.navigation.AppRoute
import com.louis.app.cavity.ui.navigation.HomeRoute
import com.louis.app.cavity.util.save
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

sealed interface HomeEvent {
    data class Navigation(val appRoute: AppRoute) : HomeEvent
    data class ScrollToCounty(val countyId: Long) : HomeEvent
}

data class LastWineChange(val wineId: Long, val countyId: Long)

data class HomeUiState(
    val observedCounty: CountyDetails? = null,
    val lastWineChange: LastWineChange? = null,
    val nonEmptyCounties: List<County> = emptyList(),
    val storageLocations: List<String> = emptyList(),
    val storageLocation: String? = null,
    val showStorageDialog: Boolean = false,
    val transitionReady: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    app: Application,
    private val countyRepository: CountyRepository,
    private val wineRepository: WineRepository,
    bottleRepository: BottleRepository,
    prefsRepository: PrefsRepository,
    private val getCountyDetails: GetCountyDetails,
    private val errorReporter: ErrorReporter,
    savedStateHandle: SavedStateHandle
) :
    BaseViewModel<HomeUiState, HomeEvent>(app, HomeUiState()) {

    private val _storageLocation = MutableStateFlow<String?>(null)
    private val _observedCountyId = MutableStateFlow<Long?>(null)
    private val _lastWineChange = MutableStateFlow<LastWineChange?>(null)
    private val _pendingScrollCountyId = MutableStateFlow<Long?>(null)

    /**
     * countyId from the arguments of the fragment that initiated a navigation shared element
     * transition to FragmentBottleDetails. Used to remember what fragment of the ViewPager we
     * should focus to resume postponed FragmentWinesParent transition
     */
    var savedSharedElementCountyId: Long? by savedStateHandle save "sourceCountyId"

    private val nonEmptyCounties =
        combine(
            _storageLocation,
            prefsRepository.enableBottleStorageLocation
        ) { location, enabled ->
            location.takeIf { enabled }
        }.flatMapLatest { location ->
            countyRepository.getNonEmptyCountiesFlow(location)
        }

    private val observedCounty =
        combine(_observedCountyId, _storageLocation) { id, location ->
            id to location
        }
            .distinctUntilChanged()
            .flatMapLatest { (countyId, location) ->
                countyId?.let {
                    getCountyDetails(it, location)
                } ?: flowOf(null)
            }

    private val storageLocations = prefsRepository.enableBottleStorageLocation
        .flatMapLatest { enabled ->
            if (enabled) {
                bottleRepository.getAllStorageLocationsFlow()
            } else {
                flowOf(emptyList())
            }
        }

    private val uiStateFlow = combine(
        _storageLocation,
        nonEmptyCounties,
        observedCounty,
        storageLocations,
        _lastWineChange
    ) { location, counties, observedCounty, locations, lastWineChange ->
        HomeUiState(
            storageLocation = location,
            nonEmptyCounties = counties,
            observedCounty = observedCounty,
            storageLocations = locations,
            showStorageDialog = locations.isNotEmpty(),
            lastWineChange = lastWineChange
        )
    }

    init {
        nonEmptyCounties
            .onEach(::handleCountyChange)
            .launchIn(viewModelScope)

        uiStateFlow
            .onEach { viewState = it }
            .launchIn(viewModelScope)

        combine(
            _pendingScrollCountyId,
            nonEmptyCounties
        ) { pendingId: Long?, counties: List<County> -> pendingId to counties }
            .onEach { (pendingId, counties) ->
                val targetId = pendingId ?: return@onEach

                if (counties.any { it.id == targetId }) {
                    _pendingScrollCountyId.value = null
                    emitEvent(HomeEvent.ScrollToCounty(targetId))
                }
            }
            .launchIn(viewModelScope)
    }

    fun notifyWineChange(wineId: Long, countyId: Long) {
        _lastWineChange.value = LastWineChange(wineId, countyId)
    }

    fun acknowledgeWineChange() {
        _lastWineChange.value = null
    }

    fun setObservedCounty(countyId: Long) {
        _observedCountyId.value = countyId
    }

    fun setStorageLocation(bottleStorage: String?, currentCountyId: Long?) {
        _pendingScrollCountyId.value = currentCountyId
        _storageLocation.value = bottleStorage
    }

    private fun handleCountyChange(counties: List<County>) {
        val targetId = _pendingScrollCountyId.value ?: return

        if (counties.any { it.id == targetId }) {
            _pendingScrollCountyId.value = null
            emitEvent(HomeEvent.ScrollToCounty(targetId))
        }
    }

    fun getWinesWithBottlesByCounty(countyId: Long): Flow<List<WineWithBottles>> {
        if (countyId < 1) {
            errorReporter.captureMessage("Illegal county id: $countyId")
        }

        return wineRepository.getWinesWithBottlesByCountyFlow(countyId)
            .combine(_storageLocation) { winesWithBottles, location ->
                if (location == null) winesWithBottles
                else winesWithBottles.filter { wineWithBottles ->
                    wineWithBottles.remainingBottles > 0 &&
                            wineWithBottles.bottles.any { it.storageLocation == location }
                }
            }
    }

    fun notifyWineObservingStarted(countyId: Long) {
        val noPendingSharedElement = savedSharedElementCountyId == null
        val isAssociatedCounty = savedSharedElementCountyId == countyId

        if (isAssociatedCounty || noPendingSharedElement) {
            viewState = viewState.copy(transitionReady = true)
        }
    }

    fun acknowledgeTransition() {
        viewState = viewState.copy(transitionReady = false)
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

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val countyRepository = CountyRepository.getInstance(app)
                val wineRepository = WineRepository.getInstance(app)
                val bottleRepository = BottleRepository.getInstance(app)
                val prefsRepository = PrefsRepository.getInstance(app)
                val statQueries = RoomStatsQueries(app)
                val getCountyDetails = GetCountyDetails(statQueries)
                val errorReporter = ErrorReporterFactory.create(app)
                val savedState = createSavedStateHandle()

                HomeViewModel(
                    app,
                    countyRepository,
                    wineRepository,
                    bottleRepository,
                    prefsRepository,
                    getCountyDetails,
                    errorReporter,
                    savedState
                )
            }
        }
    }
}
