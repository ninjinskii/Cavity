package com.louis.app.cavity.ui.navigation

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment

val Fragment.navigator: Navigator
    get() = (activity as? NavigationProvider)?.navigator
        ?: throw IllegalStateException(
            "Fragment's $this activity should implement " +
                    "${NavigationProvider::class.java.name} if you want to use navigator"
        )

fun Fragment.navigate(route: AppRoute, sharedElement: View? = null) {
    navigator.navigate(route, this, sharedElement)
}

fun Fragment.popBackStack() {
    navigator.popBackStack(this)
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
        val result: T? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable("$requestKey-result", T::class.java)
        } else {
            // Prior android version support
            @Suppress("DEPRECATION")
            bundle.getParcelable("$requestKey-result")
        }

        onResult(result)
    }
}

fun <T : Parcelable> Fragment.putFragmentResult(requestKey: String, result: T) {
    val result = Bundle().apply { putParcelable("$requestKey-result", result) }
    parentFragmentManager.setFragmentResult(requestKey, result)
}
