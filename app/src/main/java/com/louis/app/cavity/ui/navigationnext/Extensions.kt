package com.louis.app.cavity.ui.navigationnext

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import com.louis.app.cavity.ui.navigation.AppRoute
import com.louis.app.cavity.ui.navigation.NavigationProvider
import com.louis.app.cavity.util.getParcelableCompat

val Fragment.navigator: Navigator
    get() = (activity as? NavigationProvider)?.navigator
        ?: throw IllegalStateException(
            "Fragment's $this activity should implement " +
                    "${NavigationProvider::class.java.name} if you want to use navigator"
        )

fun Fragment.navigate(route: AppRoute, sharedElement: View? = null) {
    navigator.navigate(route, this, sharedElement)
}

fun Fragment.navigate2(direction: NavDirections, sharedElement: View? = null) {
    navigator.navigate2(direction, this, sharedElement)
}

fun Fragment.popBackStack() {
    navigator.popBackStack(this)
}

fun Fragment.popUpTo(destinationId: Int, inclusive: Boolean) {
    navigator.popUpTo(this, destinationId, inclusive)
}

fun Fragment.navigateUp() {
    navigator.navigateUp(this)
}

inline fun <reified T : Parcelable> Fragment.fragmentResultListener(
    requestKey: String,
    noinline onResult: (T?) -> Unit
) {
    parentFragmentManager.setFragmentResultListener(
        requestKey,
        viewLifecycleOwner
    ) { _, bundle ->
        val result = bundle.getParcelableCompat<T>("$requestKey-result")
        onResult(result)
    }
}

fun <T : Parcelable> Fragment.putFragmentResult(requestKey: String, result: T) {
    val result = Bundle().apply { putParcelable("$requestKey-result", result) }
    parentFragmentManager.setFragmentResult(requestKey, result)
}
