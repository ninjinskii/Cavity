package com.louis.app.cavity.ui.navigation

import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
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
    abstract fun popUpTo(fragment: Fragment, destinationId: Int, inclusive: Boolean)
    abstract fun getPrimaryNavigationFragment(activity: FragmentActivity): Fragment?

    // In a native environment, host fragment and primary navigation fragment should be the same
    // but, it is not the case with navigation component
    open fun getHostFragment(activity: FragmentActivity): Fragment? {
        return getPrimaryNavigationFragment(activity)
    }
}

class NavComponentNavigator(resolvers: List<RouteResolver>) :
    AppNavigator(resolvers) {

    override fun navigateUp(fragment: Fragment) {
        fragment.findNavController().navigateUp()
    }

    override fun popBackStack(fragment: Fragment) {
        fragment.findNavController().popBackStack()
    }

    override fun popUpTo(
        fragment: Fragment,
        destinationId: Int,
        inclusive: Boolean
    ) {
        fragment.findNavController().popBackStack(destinationId, inclusive)
    }

    override fun getPrimaryNavigationFragment(activity: FragmentActivity): Fragment? {
        return activity.supportFragmentManager.primaryNavigationFragment
            ?.childFragmentManager
            ?.primaryNavigationFragment
    }

    override fun getHostFragment(activity: FragmentActivity): Fragment? {
        return activity.supportFragmentManager.primaryNavigationFragment
    }
}
