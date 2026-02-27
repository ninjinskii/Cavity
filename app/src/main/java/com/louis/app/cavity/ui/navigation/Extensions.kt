package com.louis.app.cavity.ui.navigation

import androidx.fragment.app.Fragment

val Fragment.navigator: Navigator
    get() = (activity as? NavigationProvider)?.navigator
        ?: throw IllegalStateException(
            "Fragment's $this activity should implement " +
                    "${NavigationProvider::class.java.name} if you want to use navigator"
        )

fun Fragment.navigate(route: AppRoute) {
    navigator.navigate(route, this)
}

fun Fragment.popBackStack() {
    navigator.popBackStack(this)
}

fun Fragment.navigateUp() {
    navigator.navigateUp(this)
}
