package com.louis.app.cavity.ui.stats

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.louis.app.cavity.db.dao.Year
import com.louis.app.cavity.domain.delegates.GetCountyDetails
import com.louis.app.cavity.domain.error.SentryErrorReporter
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.CountyRepository
import com.louis.app.cavity.domain.repository.HistoryRepository
import com.louis.app.cavity.domain.repository.PrefsRepository
import com.louis.app.cavity.domain.repository.StatsRepository
import com.louis.app.cavity.domain.repository.WineRepository
import com.louis.app.cavity.domain.stats.RoomStatsQueries
import com.louis.app.cavity.domain.stats.Stat
import com.louis.app.cavity.domain.stats.StatSlot
import com.louis.app.cavity.domain.stats.StatType
import com.louis.app.cavity.domain.stats.StatsQueries
import com.louis.app.cavity.ui.BaseViewModel
import com.louis.app.cavity.ui.home.HomeViewModel
import com.louis.app.cavity.util.save
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class StatsUiState(
    val currentStatSlot: StatSlot = StatSlot.COUNTY,
    val currentStatType: StatType = StatType.STOCK,
    val comparison: Boolean = false
) {
    val showYearSpanOptions: Boolean
        get() = currentStatType != StatType.STOCK
}

class StatsViewModel(
    app: Application, statQueries: StatsQueries
) :
    BaseViewModel<StatsUiState, Nothing>(app, StatsUiState()) {

    private val statsRepository = StatsRepository.getInstance(app)
    private val historyRepository = HistoryRepository.getInstance(app)

    private val groupedYears = Year("Combiner", 0L, System.currentTimeMillis())
    private val _year = MutableStateFlow(groupedYears)
    private val _comparisonYear = MutableStateFlow(groupedYears)

    private val statFactory = FlowStatsFactory(statQueries, _year, _comparisonYear)

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

    fun getResults(statSlot: StatSlot): Flow<List<Stat>> = statFactory.getResults(statSlot)

    fun getComparisons(statSlot: StatSlot): Flow<List<Stat>> = statFactory.getComparisons(statSlot)

    fun getTotalPriceByCurrency() = statsRepository.getTotalPriceByCurrency()

    fun getTotalConsumed() = statsRepository.getTotalConsumedBottles()

    fun getTotalStock() = statsRepository.getTotalStockBottles()

    fun setStatType(statSlot: StatSlot, statType: StatType) {
        statFactory.applyStatType(statSlot, statType)
        viewState = viewState.copy(
            currentStatType = statType,
            currentStatSlot = statSlot,
            comparison = if (statType == StatType.STOCK) false else viewState.comparison
        )
    }

    fun setIncludeGifts(statSlot: StatSlot, includeGifts: Boolean) {
        statFactory.applyIncludeGifts(statSlot, includeGifts)
    }

    fun setStatSlot(statSlot: StatSlot) {
        viewState = viewState.copy(currentStatSlot = statSlot)

        val currentType = statFactory.getStatType(statSlot)

        viewState = viewState.copy(
            currentStatType = currentType,
            comparison = if (currentType == StatType.STOCK) false else viewState.comparison
        )
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

    @StringRes
    fun getStatTypeLabel(): Int {
        return statFactory.getStatTypeLabel(viewState.currentStatSlot)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val statQueries = RoomStatsQueries(app)

                StatsViewModel(app, statQueries)
            }
        }
    }
}


