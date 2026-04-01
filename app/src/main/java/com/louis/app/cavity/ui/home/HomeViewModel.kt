package com.louis.app.cavity.ui.home

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.louis.app.cavity.db.dao.BaseStat
import com.louis.app.cavity.db.dao.PriceByCurrency
import com.louis.app.cavity.db.dao.WineWithBottles
import com.louis.app.cavity.domain.error.ErrorReporter
import com.louis.app.cavity.domain.error.SentryErrorReporter
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.CountyRepository
import com.louis.app.cavity.domain.repository.PrefsRepository
import com.louis.app.cavity.domain.repository.StatsRepository
import com.louis.app.cavity.domain.repository.WineRepository
import com.louis.app.cavity.model.County
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
    object WinesObservingStarted : HomeEvent
    data class ScrollToCounty(val index: Int) : HomeEvent
}

data class LastWineChange(val wineId: Long, val countyId: Long)
data class ObservedCounty(
    val bottleCount: Int,
    val bottlePrice: List<PriceByCurrency>,
    val namingCount: List<BaseStat>,
    val vintagesCount: List<BaseStat>
)

data class HomeState(
    val observedCounty: ObservedCounty? = null,
    val lastWineChange: LastWineChange? = null,
    val nonEmptyCounties: List<County> = emptyList(),
    val storageLocations: List<String> = emptyList(),
    val storageLocation: String? = null,
    val toolbarTitle: String? = null,
    val showStorageDialog: Boolean = false
)

class HomeViewModel(
    app: Application,
    private val countyRepository: CountyRepository,
    private val wineRepository: WineRepository,
    private val bottleRepository: BottleRepository,
    private val statsRepository: StatsRepository,
    private val prefsRepository: PrefsRepository,
    private val errorReporter: ErrorReporter,
    savedStateHandle: SavedStateHandle
) :
    BaseViewModel<HomeState, HomeEvent>(app, HomeState()) {

    private val _storageLocation = MutableStateFlow<String?>(null)

    private val _observedCountyId = MutableStateFlow<Long?>(null)

    private val _lastWineChange = MutableStateFlow<LastWineChange?>(null)

    private var countyIdBeforeStorageLocationChange: Long? = null

    /**
     * countyId from the arguments of the fragment that initiated a navigation shared element
     * transition to FragmentBottleDetails. Used to remember what fragment of the ViewPager we
     * should focus to resume postponed FragmentWinesParent transition
     */
    var savedSharedElementCountyId: Long? by savedStateHandle save "sourceCountyId"

    @OptIn(ExperimentalCoroutinesApi::class)
    private val nonEmptyCountiesFlow = _storageLocation
        .map { location -> if (prefsRepository.getEnableBottleStorageLocation()) location else null }
        .flatMapLatest { location -> countyRepository.getNonEmptyCountiesFlow(location) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val observedCountyFlow = combine(_observedCountyId, _storageLocation) { id, location ->
        id to location
    }
        .distinctUntilChanged()
        .flatMapLatest { (countyId, location) ->
            if (countyId == null) {
                return@flatMapLatest flowOf(null)
            }

            combine(
                statsRepository.getBottleCountForCounty(countyId, location),
                statsRepository.getPriceByCurrencyForCounty(countyId, location),
                statsRepository.getNamingsStatsForCounty(countyId, location),
                statsRepository.getVintagesStatsForCounty(countyId, location)
            ) { bottleCount, bottlePrice, namingCount, vintagesCount ->
                ObservedCounty(
                    bottleCount = bottleCount,
                    bottlePrice = bottlePrice,
                    namingCount = namingCount,
                    vintagesCount = vintagesCount
                )
            }
        }

    private val storageLocationsFlow =
        bottleRepository.getAllStorageLocationsFlow()
            .takeIf { prefsRepository.getEnableBottleStorageLocation() }
            ?: flowOf(emptyList())

    init {
        combine(
            _storageLocation,
            nonEmptyCountiesFlow.onEach { checkRememberedCountyBeforeStorageChange(it) },
            observedCountyFlow,
            storageLocationsFlow,
            _lastWineChange
        ) { location, counties, observedCounty, locations, lastWineChange ->
            HomeState(
                storageLocation = location,
                toolbarTitle = location,
                nonEmptyCounties = counties,
                observedCounty = observedCounty,
                storageLocations = locations,
                showStorageDialog = locations.isNotEmpty(),
                lastWineChange = lastWineChange
            )
        }
            .onEach { viewState = it }
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
        countyIdBeforeStorageLocationChange = currentCountyId
        _storageLocation.value = bottleStorage
    }

    private fun checkRememberedCountyBeforeStorageChange(counties: List<County>) {
        counties
            .indexOfFirst { county -> county.id == countyIdBeforeStorageLocationChange }
            .let { index ->
                if (index >= 0) {
                    countyIdBeforeStorageLocationChange = null
                    emitEvent(HomeEvent.ScrollToCounty(index))
                }
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

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val countyRepository = CountyRepository.getInstance(app)
                val wineRepository = WineRepository.getInstance(app)
                val bottleRepository = BottleRepository.getInstance(app)
                val statsRepository = StatsRepository.getInstance(app)
                val prefsRepository = PrefsRepository.getInstance(app)
                val errorReporter = SentryErrorReporter.getInstance(app)
                val savedState = createSavedStateHandle()

                HomeViewModel(
                    app,
                    countyRepository,
                    wineRepository,
                    bottleRepository,
                    statsRepository,
                    prefsRepository,
                    errorReporter,
                    savedState
                )
            }
        }
    }
}
