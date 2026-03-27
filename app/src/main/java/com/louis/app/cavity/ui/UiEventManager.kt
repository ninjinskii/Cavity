package com.louis.app.cavity.ui

import android.app.Application
import com.louis.app.cavity.domain.error.SentryErrorReporter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object UiEventManager {
    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    var app: Application? = null

    fun send(event: UiEvent) {
        val success = _events.tryEmit(event)

        if (!success) {
            app?.let {
                SentryErrorReporter.getInstance(it).captureMessage(
                    "UiEventManager missed an event due to shared flow buffer overflow"
                )
            }
        }
    }
}
