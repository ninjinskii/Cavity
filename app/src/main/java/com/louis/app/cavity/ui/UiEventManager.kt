package com.louis.app.cavity.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Used by various view models to send event to SharedViewModel
object UiEventManager {
    private val _events = MutableSharedFlow<UiEvent>(
        extraBufferCapacity = 1
    )

    val events = _events.asSharedFlow()

    fun send(event: UiEvent) {
        _events.tryEmit(event)
    }
}
