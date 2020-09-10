package com.louis.app.cavity.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar

// Boolean and Int helpers for database compatibility
fun Int.toBoolean() = this == 1

fun Int.toggleBoolean() = if (this == 1) 0 else 1

fun Boolean.toInt() = if (this) 1 else 0

// View related
fun View.setVisible(isVisible: Boolean, invisible: Boolean = false) {
    visibility = if (isVisible) View.VISIBLE else if(invisible) View.INVISIBLE else View.GONE
}

fun Context.hideKeyboard() {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.toggleSoftInput(
        InputMethodManager.SHOW_IMPLICIT,
        InputMethodManager.HIDE_IMPLICIT_ONLY
    )
}

fun Context.showKeyboard(view: View) {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.toggleSoftInput(
        InputMethodManager.SHOW_IMPLICIT,
        InputMethodManager.HIDE_IMPLICIT_ONLY
    )
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

// LiveData
fun <T> MutableLiveData<Event<T>>.postOnce(value: T) {
    this.postValue(Event(value))
}

operator fun <T> MutableLiveData<MutableList<T>>.plusAssign(item: T) {
    val value = this.value ?: mutableListOf()
    value.add(0, item)
    this.value = value // notify observers
}

operator fun <T> MutableLiveData<MutableList<T>>.minusAssign(item: T) {
    this.value?.let {
        it.remove(item)
        this.value = value // notify observers
    }
}

fun <T> MutableLiveData<MutableList<T>>.clearList() {
    this.value = mutableListOf()
}

