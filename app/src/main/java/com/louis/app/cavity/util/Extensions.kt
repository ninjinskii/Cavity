package com.louis.app.cavity.util

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.use
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.louis.app.cavity.R
import com.louis.app.cavity.db.dao.PriceByCurrency
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
    Snackbar.make(this, stringRes, 3000).apply {
        actionStringRes?.let { setAction(it, action).duration = 9000 }
        anchorView?.let { this.anchorView = anchorView }
        show()
    }
}

fun CoordinatorLayout.showSnackbar(
    string: String,
    @StringRes actionStringRes: Int? = null,
    anchorView: View? = null,
    action: (View) -> Unit = { }
) {
    Snackbar.make(this, string, 3000).apply {
        actionStringRes?.let { setAction(it, action).duration = 9000 }
        anchorView?.let { this.anchorView = anchorView }
        show()
    }
}

inline fun View.doOnEachNextLayout(crossinline action: (view: View) -> Unit) {
    addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
        action(view)
    }
}

fun NestedScrollView.isViewVisible(view: View): Boolean {
    val scrollBounds = Rect()
    getHitRect(scrollBounds)

    return view.getLocalVisibleRect(scrollBounds)
}

fun Context.dpToPx(dp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
}

// Took care of it for Adroid API >= 34
@Suppress("DEPRECATION")
fun Context.pxToSp(px: Int): Float {
    val isAndroid34 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    return if (isAndroid34) {
        px / TypedValue.deriveDimension(
            TypedValue.COMPLEX_UNIT_SP,
            px.toFloat(),
            resources.displayMetrics
        )
    } else {
        px / resources.displayMetrics.scaledDensity
    }
}


fun Context.spToPx(sp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
}

@ColorInt
fun Context.themeColor(
    @AttrRes themeAttrId: Int
): Int {
    return obtainStyledAttributes(intArrayOf(themeAttrId))
        .use { it.getColor(0, Color.MAGENTA) }
}

@Suppress("UNCHECKED_CAST")
fun <T> ChipGroup.collectAs() = checkedChipIds.map {
    findViewById<Chip>(it).getTag(R.string.tag_chip_id) as T
}

@Suppress("UNCHECKED_CAST")
fun <T> ChipGroup.collectAsSingle() =
    findViewById<Chip>(checkedChipId)?.getTag(R.string.tag_chip_id) as T?

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
    combiner: (A, B) -> Result
): LiveData<Result> {
    val result = MediatorLiveData<Result>()
    result.addSource(this) { a ->
        val b = other.value
        if (b != null) {
            result.value = combiner(a, b)
        }
    }
    result.addSource(other) { b ->
        val a = this@combine.value
        if (a != null) {
            result.value = combiner(a, b)
        }
    }
    return result
}

fun <A, B, Result> LiveData<A>.combineAsync(
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
        val a = this@combineAsync.value
        if (a != null) {
            combiner(result, a, b)
        }
    }
    return result
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
fun Fragment.setupNavigation(toolbar: Toolbar, hideDrawerToggle: Boolean = false) {
    if (!hideDrawerToggle) {
        val drawer = (activity as ActivityMain).findViewById<DrawerLayout>(R.id.drawer)
        val navController = findNavController()
        toolbar.setupWithNavController(navController, drawer)
    } else {
        toolbar.title = getString(R.string.app_name)
        toolbar.setNavigationOnClickListener(null)
    }
}

// Random
fun List<PriceByCurrency>.join(): String {
    val builder = StringBuilder("")

    forEachIndexed { index, priceByCurrency ->
        if (index == size - 1) {
            builder.append(priceByCurrency.toString())
        } else {
            builder.append("$priceByCurrency - ")
        }
    }

    return builder.toString()
}
