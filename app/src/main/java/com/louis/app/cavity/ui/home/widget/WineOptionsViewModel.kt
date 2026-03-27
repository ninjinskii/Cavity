package com.louis.app.cavity.ui.home.widget

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.delegates.RemoveWine
import com.louis.app.cavity.domain.delegates.RemoveWineUseCase
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.WineRepository
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.ui.BaseViewModel
import com.louis.app.cavity.ui.UiEvent
import com.louis.app.cavity.ui.UiEventManager
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class WineOptionsState(val wine: Wine? = null)

class WineOptionsViewModel(
    app: Application,
    private val removeWine: RemoveWineUseCase,
    wineRepository: WineRepository,
    savedStateHandle: SavedStateHandle
) :
    BaseViewModel<WineOptionsState, Nothing>(app, WineOptionsState()) {

    private val wineId: Long = checkNotNull(savedStateHandle["wineId"])
    /*val stateFlow = listWineUseCase.getWine(wineId)
        .map { WineOptionsState(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            WineOptionsState()
        )*/

    init {
        wineRepository.getWineByIdFlow(wineId)
            .map { WineOptionsState(it) }
            .onEach { viewState = it }
            .launchIn(viewModelScope)

        /*viewModelScope.launch(IO) {
            listWineUseCase.getWine(wineId).collect { wine ->
                stateFlow.update {
                    it.copy(wine = wine)
                }

        }*/
    }

    fun handleWineDeleteRequest() {
        viewModelScope.launch(IO) {
            val success = removeWine(wineId)
            val message = if (success) R.string.wine_deleted else R.string.base_error
            UiEventManager.send(UiEvent.Snackbar(message))
        }
    }

    companion object {
        // We can use a lambda right before "viewModelFactory" if we really need to provide an argument to the view model
        // But fragment arguments can be retrieved in the saved state handle
        val Factory = viewModelFactory {
            initializer {
                // We may be able to discard app usage if repositories are not singleton anymore
                val app = checkNotNull(this[APPLICATION_KEY])
                val bottleRepository = BottleRepository.getInstance(app)
                val wineRepository = WineRepository.getInstance(app)
                val wineRemover = RemoveWine(wineRepository, bottleRepository)
                val savedState = createSavedStateHandle()
                WineOptionsViewModel(app, wineRemover, wineRepository, savedState)
            }
        }
    }
}
