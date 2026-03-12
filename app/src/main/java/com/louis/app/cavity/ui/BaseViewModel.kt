package com.louis.app.cavity.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class BaseViewModel<State, Event>(app: Application, defaultState: State) :
    AndroidViewModel(app) {

    val state: StateFlow<State> get() = stateFlow
    val event: SharedFlow<Event> get() = eventFlow

    protected open val stateFlow: MutableStateFlow<State> by lazy { MutableStateFlow(defaultState) }
    protected val eventFlow: MutableSharedFlow<Event> = MutableSharedFlow(extraBufferCapacity = 1)

    var viewState: State
        get() = state.value
        set(state) {
            stateFlow.value = state
        }

    protected fun emitEvent(event: Event) {
        viewModelScope.launch {
            eventFlow.emit(event)
        }
    }
}
