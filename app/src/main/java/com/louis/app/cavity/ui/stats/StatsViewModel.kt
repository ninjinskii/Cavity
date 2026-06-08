package com.louis.app.cavity.ui.stats

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.louis.app.cavity.domain.stats.StatsYearTimeSpan
import com.louis.app.cavity.domain.repository.HistoryRepository
import com.louis.app.cavity.domain.stats.RoomStatsQueries
import com.louis.app.cavity.domain.stats.Stat
import com.louis.app.cavity.domain.stats.StatGroupBy
import com.louis.app.cavity.domain.stats.InventoryStatFilter
import com.louis.app.cavity.domain.stats.StatsQueries
import com.louis.app.cavity.ui.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class StatsUiState(
    val statGroupBy: StatGroupBy = StatGroupBy.COUNTY,
    val inventoryStatFilter: InventoryStatFilter = InventoryStatFilter.Stock,
    val comparison: Boolean = false,
    val statsTimeSpans: List<StatsYearTimeSpan> = emptyList(),
    val comparisonText: String = ""
) {
    val showYearSpanOptions: Boolean
        get() = inventoryStatFilter.supportsYearFiltering
}

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    app: Application,
    statQueries: StatsQueries,
    historyRepository: HistoryRepository
) :
    BaseViewModel<StatsUiState, Nothing>(app, StatsUiState()) {

    private val allYears = StatsYearTimeSpan(0, 0L, System.currentTimeMillis())
    private val statFactory = FlowStatsFactory(statQueries)

    private val statFilters =
        MutableStateFlow(
            StatFilters(
                statGroupBy = StatGroupBy.COUNTY,
                statsTimeSpan = allYears,
                comparisonStatsTimeSpan = allYears,
                statRequests = StatGroupBy.entries.associateWith {
                    StatRequest(
                        inventoryStatFilter = InventoryStatFilter.Stock,
                        includeGifts = false
                    )
                }
            )
        )

    private val years: Flow<List<StatsYearTimeSpan>> =
        historyRepository.getYears().map {
            it.toMutableList().apply {
                reverse()
                add(0, allYears)
                add(allYears)
            }
        }

    val uiStateFlow: Flow<StatsUiState> =
        combine(
            statFilters,
            years
        ) { filters, years ->
            StatsUiState(
                statGroupBy = filters.statGroupBy,
                inventoryStatFilter = filters.statRequests.getValue(filters.statGroupBy).inventoryStatFilter,
                comparison = filters.comparisonStatsTimeSpan != allYears,
                statsTimeSpans = years,
                comparisonText = "${filters.statsTimeSpan} <> ${filters.comparisonStatsTimeSpan}"
            )
        }

    init {
        uiStateFlow
            .onEach { viewState = it }
            .launchIn(viewModelScope)
    }

    private fun statRequest(statGroupBy: StatGroupBy): Flow<StatRequest> =
        statFilters.map { it.statRequests.getValue(statGroupBy) }

    fun pieResults(statGroupBy: StatGroupBy): Flow<Pair<List<Stat>, Boolean>> =
        combine(statFilters, statRequest(statGroupBy)) { filters, request ->
            (filters to request)
        }
            .flatMapLatest { (filters, request) ->
                statFactory.getResults(
                    statGroupBy = statGroupBy,
                    timeSpan = filters.statsTimeSpan,
                    inventoryStatFilter = request.inventoryStatFilter,
                    includeGifts = request.includeGifts
                )
                    .map { stats ->
                        println("emit ${request.includeGifts}")
                        stats to request.includeGifts
                    }
            }
            .distinctUntilChanged()

    fun pieComparisons(statGroupBy: StatGroupBy): Flow<List<Stat>> =
        combine(statFilters, statRequest(statGroupBy)) { filters, request ->
            filters to request
        }
            .flatMapLatest { (filters, request) ->
                statFactory.getComparisons(
                    statGroupBy = statGroupBy,
                    comparisonStatsTimeSpan = filters.comparisonStatsTimeSpan,
                    inventoryStatFilter = request.inventoryStatFilter,
                    includeGifts = request.includeGifts
                )
            }
            .distinctUntilChanged()

    fun setStatFilter(statGroupBy: StatGroupBy, inventoryStatFilter: InventoryStatFilter) {
        statFilters.update { current ->
            current.copy(
                statRequests = current.statRequests.toMutableMap().apply {
                    this[statGroupBy] =
                        getValue(statGroupBy).copy(
                            inventoryStatFilter = inventoryStatFilter
                        )
                }
            )
        }
    }

    fun setIncludeGifts(statGroupBy: StatGroupBy, includeGifts: Boolean) {
        println("setIncludeGifts $statGroupBy -> $includeGifts")
        statFilters.update { current ->
            current.copy(
                statRequests = current.statRequests + (
                        statGroupBy to current.statRequests
                            .getValue(statGroupBy)
                            .copy(includeGifts = includeGifts)
                        )
            )
        }
    }

    fun setStatSlot(statGroupBy: StatGroupBy) {
        statFilters.update {
            it.copy(statGroupBy = statGroupBy)
        }
    }

    fun setYear(statsTimeSpan: StatsYearTimeSpan) {
        statFilters.update {
            it.copy(statsTimeSpan = statsTimeSpan)
        }
    }

    fun setComparisonYear(statsTimeSpan: StatsYearTimeSpan) {
        statFilters.update {
            it.copy(comparisonStatsTimeSpan = statsTimeSpan)
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

    private data class StatFilters(
        val statGroupBy: StatGroupBy,
        val statsTimeSpan: StatsYearTimeSpan,
        val comparisonStatsTimeSpan: StatsYearTimeSpan,
        val statRequests: Map<StatGroupBy, StatRequest>
    )

    private data class StatRequest(
        val inventoryStatFilter: InventoryStatFilter,
        val includeGifts: Boolean
    )

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val statQueries = RoomStatsQueries(app)
                val historyRepository = HistoryRepository.getInstance(app)

                StatsViewModel(app, statQueries, historyRepository)
            }
        }
    }
}
