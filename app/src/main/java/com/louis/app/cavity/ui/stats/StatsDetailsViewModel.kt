package com.louis.app.cavity.ui.stats

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.dao.BoundedBottle
import com.louis.app.cavity.domain.repository.StatsRepository
import com.louis.app.cavity.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class StatsDetailsUiState(val bottles: List<BoundedBottle> = emptyList())

class StatsDetailsViewModel(app: Application) : BaseViewModel<StatsDetailsUiState, Nothing>(app, StatsDetailsUiState()) {
    private val repository = StatsRepository.getInstance(app)

    private val _bottlesIds = MutableStateFlow<List<Long>>(emptyList())

    init {
        _bottlesIds
            .flatMapLatest { repository.getBottlesByIds(it) }
            .onEach { viewState = viewState.copy(bottles = it) }
            .launchIn(viewModelScope)
    }

    fun setBottlesIds(ids: List<Long>) {
        _bottlesIds.value = ids
    }
}
