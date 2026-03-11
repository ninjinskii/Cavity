package com.louis.app.cavity.ui.navigation

import android.content.Context
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle

class Navigator(private val activity: AppCompatActivity) {
    private val menus = mutableListOf<Menu>()
    private val transitionExecutor: TransitionExecutor = MaterialTransitionExecutor()
    private val appNavigator: AppNavigator by lazy {
        NavComponentNavigator(
            resolvers = listOf(
                NavComponentGlobalRouteResolver(),
                NavComponentHomeRouteResolver(),
                NavComponentWineOptionsRouteResolver(),
                NavComponentSearchRouteResolver(),
                NavComponentAddWineRouteResolver()
            )
        )
    }

    fun navigate(route: AppRoute, sharedElement: View? = null) {
        val navFragment = getCurrentFragment()
            ?: throw IllegalStateException("Unable to find primary navigation fragment from Activity: $activity")

        navigateInternal(route, navFragment, sharedElement)
    }

    fun navigate(route: AppRoute, fragment: Fragment, sharedElement: View? = null) {
        if (getCurrentFragment() != fragment) {
            throw IllegalStateException("On pose ça là pour voir si un jour on trouve une diff entre les deux mais on pense que ça devrait pas arriver")
        }

        val navFragment = getCurrentFragment() ?: fragment
        navigateInternal(route, navFragment, sharedElement)
    }

    fun navigateUp(fragment: Fragment) {
        appNavigator.navigateUp(fragment)
    }

    fun popBackStack(fragment: Fragment) {
        appNavigator.popBackStack(fragment)
    }

    fun syncMenu(vararg menu: Menu) {
        this.menus.addAll(menu)
    }

    fun setup() {
        val started = activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        require(started) {
            "Navigator#setup is called before activity is STARTED. " +
                    "Current lifecycle state: ${activity.lifecycle.currentState.name}"
        }

        val hostFragment = appNavigator.getHostFragment(activity)
        hostFragment?.childFragmentManager?.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentPreAttached(
                    fragmentManager: FragmentManager,
                    fragment: Fragment,
                    context: Context
                ) {
                    transitionExecutor.configureDestinationFragment(fragment)
                }

                override fun onFragmentResumed(
                    fragmentManager: FragmentManager,
                    fragment: Fragment
                ) {
                    if (fragment is NavigationDestination) {
                        menus.forEach { it.findItem(fragment.menuDestinationId)?.isChecked = true }
                    } else {
                        menus.forEach { it.forEach { item -> item.isChecked = false } }
                    }
                }
            }, false
        )
    }

    fun getCurrentFragment(): Fragment? {
        return appNavigator.getPrimaryNavigationFragment(activity)
    }

    private fun navigateInternal(route: AppRoute, fragment: Fragment, sharedElement: View? = null) {
        transitionExecutor.configureFragment(fragment, route)
        appNavigator.navigate(route, fragment, sharedElement)
    }
}
