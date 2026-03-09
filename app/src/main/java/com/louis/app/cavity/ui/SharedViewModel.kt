package com.louis.app.cavity.ui

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.util.L
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScrollToWineRequest(val wineId: Long, val countyId: Long)
data class SharedState(val scrollToWineRequest: ScrollToWineRequest)

val defaultState = SharedState(ScrollToWineRequest(-1, -1))

sealed interface SharedEvent {
    data class WineUpdatedEvent(val wineUpdated: UiEvent.WineUpdated) : SharedEvent
}

class SharedViewModel(app: Application) :
    BaseViewModel<SharedState, SharedEvent>(app, defaultState) {

    /*private val _eventChannel = Channel<SharedEvent>(Channel.BUFFERED)
    val eventChannel: Flow<SharedEvent> get() = _eventChannel.receiveAsFlow()*/

    fun updateWineState(event: UiEvent.WineUpdated) {
        L.v("SharedViewModel: state updated $event")
        stateFlow.update {
            it.copy(scrollToWineRequest = ScrollToWineRequest(event.wineId, event.countyId))
        }
    }

    fun consumeScrollToWineRequest() {
        viewModelScope.launch {
            // We need a little delay to avoid skipping events
            delay(200)
            stateFlow.update { defaultState }
        }
    }
}
