package com.louis.app.cavity.ui

import androidx.annotation.StringRes

interface SnackbarProvider {
    fun onShowSnackbarRequested(@StringRes stringRes: Int)
}
