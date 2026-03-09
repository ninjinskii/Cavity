package com.louis.app.cavity.ui.navigation

import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

abstract class AppNavigator(private val resolvers: List<RouteResolver>) {
    @CallSuper
    open fun navigate(route: AppRoute, fragment: Fragment, sharedElement: View?) {
        val resolver = resolvers.firstOrNull { it.canHandle(route) }
            ?: throw IllegalArgumentException("No resolver for $route")

        resolver.resolve(route, fragment, sharedElement)
    }

    abstract fun navigateUp(fragment: Fragment)
    abstract fun popBackStack(fragment: Fragment)
}

class NavComponentNavigator(resolvers: List<RouteResolver>) :
    AppNavigator(resolvers) {

    override fun navigateUp(fragment: Fragment) {
        fragment.findNavController().navigateUp()
    }

    override fun popBackStack(fragment: Fragment) {
        fragment.findNavController().popBackStack()
    }
}
