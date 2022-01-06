package com.louis.app.cavity.ui

/**
 * Interface responsible to close opened dialogs if any when navigating to avoid monkey like crashes
 */
interface DialogWatcher {
    fun closeMaybeOpenedDialog()
}
