package com.louis.app.cavity.ui.stats

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.louis.app.cavity.domain.repository.HistoryRepository
import com.louis.app.cavity.domain.stats.RoomStatsQueries
import com.louis.app.cavity.domain.stats.Stat
import com.louis.app.cavity.domain.stats.StatGroupBy
import com.louis.app.cavity.domain.stats.InventoryStatFilter
import com.louis.app.cavity.domain.stats.StatsQueries
import com.louis.app.cavity.domain.stats.StatsYearTimeSpan
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

data class StatsScreenUiState(
    val selectedGroupBy: StatGroupBy,
    val selectedPage: StatPageUiState,
    val years: List<StatsYearTimeSpan>
) {
    val showYearSpanOptions: Boolean
        get() = selectedPage.inventoryStatFilter.supportsYearFiltering

    val showIncludeGivenBottlesOption: Boolean
        get() = selectedPage.inventoryStatFilter.supportsGivenBottleFiltering
}

data class StatPageUiState(
    val inventoryStatFilter: InventoryStatFilter = InventoryStatFilter.Stock,
    val statsTimeSpan: StatsYearTimeSpan = StatsYearTimeSpan.ALL_YEARS,
    val comparisonTimeSpan: StatsYearTimeSpan = StatsYearTimeSpan.ALL_YEARS,
    val includeGifts: Boolean = false
) {
    val comparisonEnabled: Boolean
        get() = comparisonTimeSpan != StatsYearTimeSpan.ALL_YEARS
}

data class GlobalStatsUiState(
    val selectedGroupBy: StatGroupBy = StatGroupBy.COUNTY,
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    statQueries: StatsQueries,
    historyRepository: HistoryRepository
) :
    ViewModel() {

    private val statFactory = FlowStatsHelper(statQueries)

    private val globalState = MutableStateFlow(GlobalStatsUiState())

    private val pages = MutableStateFlow(
        StatGroupBy.entries.associateWith { StatPageUiState() }
    )

    private val years: Flow<List<StatsYearTimeSpan>> =
        historyRepository.getYears()
            .map {
                it.toMutableList().apply {
                    reverse()
                    add(0, StatsYearTimeSpan.ALL_YEARS)
                    add(StatsYearTimeSpan.ALL_YEARS)
                }
            }

    val screenState =
        combine(
            globalState,
            pages,
            years
        ) { global, pages, years ->

            val selectedPage = pages.getValue(global.selectedGroupBy)

            StatsScreenUiState(
                selectedGroupBy = global.selectedGroupBy,
                selectedPage = selectedPage,
                years = years
            )
        }

    /** Used only by FragmentStats because it need to break the single source of truth by knowing
     * what page of view pager is being shown right now. */
    val pieResultsFlow = screenState.map { it.selectedGroupBy }
        .flatMapLatest { groupBy ->
            pieResults(groupBy)
        }


    fun pageState(statGroupBy: StatGroupBy): Flow<StatPageUiState> =
        pages
            .map { it.getValue(statGroupBy) }
            .distinctUntilChanged()

    fun pieResults(statGroupBy: StatGroupBy): Flow<List<Stat>> =
        pageState(statGroupBy).flatMapLatest { page ->
            statFactory.getResults(
                statGroupBy = statGroupBy,
                timeSpan = page.statsTimeSpan,
                inventoryStatFilter = page.inventoryStatFilter,
                includeGifts = page.includeGifts
            )
        }
            .distinctUntilChanged()

    fun totalBottlesCount(statGroupBy: StatGroupBy): Flow<Int> =
        pieResults(statGroupBy).map { stats ->
            stats.sumOf { it.count }
        }

    fun pieComparisons(statGroupBy: StatGroupBy): Flow<List<Stat>> =
        pageState(statGroupBy).flatMapLatest { page ->
            statFactory.getComparisons(
                statGroupBy = statGroupBy,
                comparisonStatsTimeSpan = page.comparisonTimeSpan,
                inventoryStatFilter = page.inventoryStatFilter,
                includeGifts = page.includeGifts
            )
        }
            .distinctUntilChanged()

    fun setStatFilter(statGroupBy: StatGroupBy, filter: InventoryStatFilter) {
        pages.update { current ->
            current.toMutableMap().apply {
                this[statGroupBy] =
                    getValue(statGroupBy).copy(
                        inventoryStatFilter = filter
                    )
            }
        }
    }

    fun setIncludeGifts(statGroupBy: StatGroupBy, includeGifts: Boolean) {
        pages.update { current ->
            current.toMutableMap().apply {
                this[statGroupBy] =
                    getValue(statGroupBy).copy(
                        includeGifts = includeGifts
                    )
            }
        }
    }

    fun setSelectedGroupBy(statGroupBy: StatGroupBy) {
        globalState.update {
            it.copy(
                selectedGroupBy = statGroupBy
            )
        }
    }

    fun setYear(statsTimeSpan: StatsYearTimeSpan) {
        val currentStatGroupBy = globalState.value.selectedGroupBy

        pages.update { current ->
            current.toMutableMap().apply {
                this[currentStatGroupBy] =
                    getValue(currentStatGroupBy).copy(
                        statsTimeSpan = statsTimeSpan
                    )
            }
        }
    }

    fun setComparisonYear(statsTimeSpan: StatsYearTimeSpan) {
        val currentStatGroupBy = globalState.value.selectedGroupBy

        pages.update { current ->
            current.toMutableMap().apply {
                this[currentStatGroupBy] =
                    getValue(currentStatGroupBy).copy(
                        comparisonTimeSpan = statsTimeSpan
                    )
            }
        }
    }

    // TODO: make this work again
    @StringRes
    fun getStatTypeLabel(): Int {
        /*val slot = uiStateFlow.value.statGroupBy
        return when (statFilters.value.statRequests.getValue(slot).inventoryStatFilter) {
            InventoryStatFilter.STOCK -> R.string.stock
            InventoryStatFilter.REPLENISHMENTS -> R.string.replenishments
            InventoryStatFilter.CONSUMPTIONS -> R.string.consumptions
        }*/
        return -1
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val statQueries = RoomStatsQueries(app)
                val historyRepository = HistoryRepository.getInstance(app)

                StatsViewModel(statQueries, historyRepository)
            }
        }
    }
}
