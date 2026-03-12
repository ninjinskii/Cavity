package com.louis.app.cavity.ui.home.widget

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.delegates.BottlesFinder
import com.louis.app.cavity.domain.delegates.WineFinder
import com.louis.app.cavity.domain.delegates.RemoveWineUseCase
import com.louis.app.cavity.domain.delegates.WineRemover
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.ui.BaseViewModel
import com.louis.app.cavity.ui.UiEvent
import com.louis.app.cavity.ui.UiEventManager
import com.louis.app.cavity.util.L
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class WineOptionsState(val currentWine: Wine? = null, val currentBottle: Bottle? = null)

class WineOptionsViewModel(
    app: Application,
    private val wineId: Long,
    private val wineRemover: WineRemover,
    private val wineFinder: WineFinder,
    private val bottlesFinder: BottlesFinder,
    private val savedStateHandle: SavedStateHandle
) :
    BaseViewModel<WineOptionsState, Nothing>(app, WineOptionsState()) {

    companion object {
        val Factory = { wineId: Long ->
            viewModelFactory {
                initializer {
                    // We may be able to discard app usage if repositories are not singleton anymore
                    val app = checkNotNull(this[APPLICATION_KEY])
                    val bottleRepository = BottleRepository.getInstance(app)
                    val wineRepository = WineRepository.getInstance(app)
                    val wineRemover = RemoveWineUseCase(wineRepository, bottleRepository)
                    val wineFinder = WineFinder(wineRepository)
                    val bottleFinder = BottlesFinder(bottleRepository)
                    val handle = createSavedStateHandle()
                    handle[SAVED_STATE_KEY] = null // wineId
                    WineOptionsViewModel(app, wineId, wineRemover, wineFinder, bottleFinder, handle)
                }
            }
        }

        private const val SAVED_STATE_KEY = "com.louis.app.cavity.ui.WineOptionsViewModel"
    }

    init {
        viewModelScope.launch(IO) {
            val wideId = savedStateHandle["wineId"] ?: wineId
            L.v("WineOptionsViewModel: ${savedStateHandle.get<Boolean>("storageLocationActive")}")
           /* wineFinder.getWine(id).collect { wine ->
                stateFlow.update { it.copy(currentWine = wine) }
            }

            bottlesFinder.getBottle(1).collect { bottle ->
                stateFlow.update { it.copy(currentBottle = bottle) }
            }*/

            combine(wineFinder.getWine(wideId), bottlesFinder.getBottle(1)) {
                wine, bottle ->
                WineOptionsState(wine, bottle)
            }.collect {
                viewState = it
            }
        }
    }

    fun handleWineDeleteRequest(wineId: Long) {
        viewModelScope.launch(IO) {
            val success = wineRemover.removeWine(wineId)
            val message = if (success) R.string.wine_deleted else R.string.base_error
            UiEventManager.send(UiEvent.Snackbar(message))
        }
    }
}
