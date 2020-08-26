package com.louis.app.cavity.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar

fun Int.toBoolean() = this == 1

fun Int.toggleBoolean() = if (this == 1) 0 else 1

fun Boolean.toInt() = if (this) 1 else 0

fun View.setVisible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun <T> MutableLiveData<Event<T>>.postOnce(value: T) {
    this.postValue(Event(value))
}

fun Context.hideKeyboard() {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

fun Context.showKeyboard(view: View) {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

fun CoordinatorLayout.showSnackbar(
    @StringRes stringRes: Int,
    @StringRes actionStringRes: Int? = null,
    action: (View) -> Unit = { }
) {
    Snackbar.make(this, stringRes, Snackbar.LENGTH_LONG).apply {
        actionStringRes?.let { setAction(it, action).duration = 8000 }
    }.show()
}
