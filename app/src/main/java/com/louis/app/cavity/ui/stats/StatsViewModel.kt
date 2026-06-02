package com.louis.app.cavity.ui.stats

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.dao.Year
import com.louis.app.cavity.domain.repository.HistoryRepository
import com.louis.app.cavity.domain.repository.StatsRepository
import com.louis.app.cavity.ui.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val currentItemPosition: Int = 0,
    val showYearSpanOptions: Boolean = false,
    val comparison: Boolean = false
)

class StatsViewModel(app: Application) : BaseViewModel<StatsUiState, Nothing>(app, StatsUiState()) {
    private val statsRepository = StatsRepository.getInstance(app)
    private val historyRepository = HistoryRepository.getInstance(app)

    private val groupedYears = Year("Combiner", 0L, System.currentTimeMillis())
    private val _year = MutableStateFlow(groupedYears)
    private val _comparisonYear = MutableStateFlow(groupedYears)

    private val statFactory = FlowStatsFactory(statsRepository, _year, _comparisonYear)

    val comparisonText = combine(_year, _comparisonYear) { year, cYear ->
        "$year <> $cYear"
    }

    val years: Flow<List<Year>> = historyRepository.getYears().map {
        it.toMutableList().apply {
            reverse()
            add(0, groupedYears)
            add(groupedYears)
        }
    }

    fun getResults(position: Int): Flow<List<com.louis.app.cavity.db.dao.Stat>> =
        statFactory.getResults(position)

    fun getComparisons(position: Int): Flow<List<com.louis.app.cavity.db.dao.Stat>> =
        statFactory.getComparisons(position)

    fun getTotalPriceByCurrency() = statsRepository.getTotalPriceByCurrency()

    fun getTotalConsumed() = statsRepository.getTotalConsumedBottles()

    fun getTotalStock() = statsRepository.getTotalStockBottles()

    fun setStatType(viewPagerPos: Int, statType: StatType) {
        statFactory.applyStatType(viewPagerPos, statType)
    }

    fun setIncludeGifts(viewPagePos: Int, includeGifts: Boolean) {
        statFactory.applyIncludeGifts(viewPagePos, includeGifts)
    }

    fun notifyPageChanged(position: Int) {
        viewState = viewState.copy(currentItemPosition = position)
    }

    fun setYear(year: Year) {
        if (year != _year.value) {
            _year.value = year
        }
    }

    fun setComparisonYear(year: Year) {
        viewState = viewState.copy(comparison = true)
        _comparisonYear.value = year
    }

    fun setShouldShowYearPicker(show: Boolean) {
        if (show != viewState.showYearSpanOptions) {
            viewState = viewState.copy(showYearSpanOptions = show)

            if (!show) {
                viewState = viewState.copy(comparison = false)
            }
        }
    }

    @StringRes
    fun getStatTypeLabel(): Int {
        return statFactory.getStatTypeLabel(viewState.currentItemPosition)
    }
}

enum class StatType {
    STOCK,
    REPLENISHMENTS,
    CONSUMPTIONS,
}


