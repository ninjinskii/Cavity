package com.louis.app.cavity.ui.stats

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.louis.app.cavity.domain.repository.StatsRepository
import com.louis.app.cavity.domain.stats.RoomStatsQueries
import com.louis.app.cavity.ui.BaseViewModel
import com.louis.app.cavity.util.join
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class CellarStatsUiState(
    val totalPriceByCurrency: String = "",
    val totalConsumedBottles: Int = 0,
    val remainingBottles: Int = 0,
)

class CellarStatsViewModel(
    app: Application, statsRepository: StatsRepository
) :
    BaseViewModel<CellarStatsUiState, Nothing>(app, CellarStatsUiState()) {

    val totalPriceByCurrency = statsRepository.getTotalPriceByCurrency()
    val totalConsumed = statsRepository.getTotalConsumedBottles()
    val totalStock = statsRepository.getTotalStockBottles()

    init {
        combine(totalPriceByCurrency, totalConsumed, totalStock) { prices, consumed, stock ->
            CellarStatsUiState(
                totalPriceByCurrency = prices.join(),
                totalConsumedBottles = consumed,
                remainingBottles = stock
            )
        }
            .onEach { viewState = it }
            .launchIn(viewModelScope)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val statsRepository = StatsRepository.getInstance(app)
                CellarStatsViewModel(app, statsRepository)
            }
        }
    }
}
