package com.louis.app.cavity.ui

import androidx.annotation.IdRes
import androidx.annotation.StringRes

sealed interface UiEvent {
    data class Snackbar(
        @param:StringRes val message: Int,
        @param:IdRes val anchorViewId: Int? = null
    ) :
        UiEvent

    data class ActionSnackbar(
        @param:StringRes val message: Int,
        @param:StringRes val actionLabel: Int,
        @param:IdRes val anchorViewId: Int? = null,
        val action: (() -> Unit)
    ) :
        UiEvent
}
