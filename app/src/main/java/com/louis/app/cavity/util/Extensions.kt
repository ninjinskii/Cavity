package com.louis.app.cavity.util

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.louis.app.cavity.ui.ActivityMain

// Boolean and Int helpers for database compatibility
fun Int.toBoolean() = this == 1

fun Int.toggleBoolean() = if (this == 1) 0 else 1

fun Boolean.toInt() = if (this) 1 else 0

// View related
fun View.setVisible(isVisible: Boolean, invisible: Boolean = false) {
    visibility = if (isVisible) View.VISIBLE else if (invisible) View.INVISIBLE else View.GONE
}

fun View.hideKeyboard() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
        ?.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard() {
    requestFocus()

    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
        ?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun CoordinatorLayout.showSnackbar(
    @StringRes stringRes: Int,
    @StringRes actionStringRes: Int? = null,
    anchorView: View? = null,
    action: (View) -> Unit = { }
) {
    Snackbar.make(this, stringRes, Snackbar.LENGTH_LONG).apply {
        actionStringRes?.let { setAction(it, action).duration = 8000 }
        anchorView?.let { this.anchorView = anchorView }
        show()
    }
}

inline fun View.doOnEachNextLayout(crossinline action: (view: View) -> Unit) {
    addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
        action(view)
    }
}

fun Context.dpToPx(dp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
}

fun Context.pxToDp(px: Int): Float {
    return px / (resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)
}

// LiveData
fun <T> MutableLiveData<Event<T>>.postOnce(value: T) {
    this.postValue(Event(value))
}

operator fun <T> MutableLiveData<MutableList<T>>.plusAssign(item: T) {
    val value = this.value ?: mutableListOf()
    value.add(value.size, item)
    this.value = value // notify observers
}

operator fun <T> MutableLiveData<MutableList<T>>.minusAssign(item: T) {
    this.value?.let {
        it.remove(item)
        this.value = value // notify observers
    }
}

fun <A, B, Result> LiveData<A>.combine(
    other: LiveData<B>,
    combiner: (MutableLiveData<Result>, A, B) -> Unit
): LiveData<Result> {
    val result = MediatorLiveData<Result>()
    result.addSource(this) { a ->
        val b = other.value
        if (b != null) {
            combiner(result, a, b)
        }
    }
    result.addSource(other) { b ->
        val a = this@combine.value
        if (a != null) {
            combiner(result, a, b)
        }
    }
    return result
}

fun <T> MutableLiveData<MutableList<T>>.clearList() {
    this.value = mutableListOf()
}

// BottomSheet
fun BottomSheetBehavior<ConstraintLayout>.isCollapsed() =
    state == BottomSheetBehavior.STATE_COLLAPSED

fun BottomSheetBehavior<ConstraintLayout>.isExpanded() = state == BottomSheetBehavior.STATE_EXPANDED

fun BottomSheetBehavior<ConstraintLayout>.toggleState() {
    state =
        if (isExpanded())
            BottomSheetBehavior.STATE_COLLAPSED
        else
            BottomSheetBehavior.STATE_EXPANDED
}

// Navigation
fun Fragment.setupNavigation(toolbar: Toolbar) {
    val act = activity as ActivityMain

    act.setSupportActionBar(toolbar)

    val navController = findNavController()
    val appBarConfiguration = AppBarConfiguration(navController.graph, act.drawer)

    toolbar.setupWithNavController(navController, appBarConfiguration)
    act.navView.setupWithNavController(navController)
}

// String
fun String.isNotBlankOrNull(): String? {
    return if (this.isBlank()) null else this
}
