package com.louis.app.cavity.ui.navigation

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.louis.app.cavity.ui.navigation.transition.MaterialFragmentTransitionManager
import kotlin.getValue

class Navigator(private val activity: AppCompatActivity) {
    private val menus = mutableListOf<Menu>()
    private val fragmentTransitionManager = MaterialFragmentTransitionManager()

    //    private val sharedViewModel by lazy { ViewModelProvider(activity, SharedViewModel.Factory)[SharedViewModel::class.java] }
    private val appNavigator: AppNavigator by lazy {
        NavComponentNavigator(
            resolvers = listOf(
                NavComponentGlobalRouteResolver(),
                NavComponentHomeRouteResolver(),
                NavComponentWineOptionsRouteResolver(),
                NavComponentSearchRouteResolver(),
                NavComponentAddWineRouteResolver(),
                NavComponentBottleDetailsRouteResolver()
            )
        )
    }

    fun navigate(route: AppRoute, sharedElement: View? = null) {
        val navFragment = getCurrentFragment()
            ?: throw IllegalStateException("Unable to find primary navigation fragment from Activity: $activity")

        navigateInternal(route, navFragment, sharedElement)
    }

    fun navigate(route: AppRoute, fragment: Fragment, sharedElement: View? = null) {
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
                override fun onFragmentViewCreated(
                    fragmentManager: FragmentManager,
                    fragment: Fragment,
                    view: View,
                    savedInstanceState: Bundle?
                ) {
                    fragmentTransitionManager.restoreState(fragment)
                }

                override fun onFragmentPreAttached(
                    fragmentManager: FragmentManager,
                    fragment: Fragment,
                    context: Context
                ) {
                    fragmentTransitionManager.configureDestinationFragment(fragment)
                }

                override fun onFragmentResumed(
                    fragmentManager: FragmentManager,
                    fragment: Fragment
                ) {
                    fragmentTransitionManager.restoreState(fragment)
                    updateMenus(fragment)
                }
            }, false
        )
    }

    fun getCurrentFragment(): Fragment? {
        return appNavigator.getPrimaryNavigationFragment(activity)
    }

    private fun updateMenus(fragment: Fragment) {
        if (fragment is NavigationDestination) {
            menus.forEach { it.findItem(fragment.menuDestinationId)?.isChecked = true }
        } else {
            menus.forEach { it.forEach { item -> item.isChecked = false } }
        }
    }

    private fun navigateInternal(route: AppRoute, fragment: Fragment, sharedElement: View? = null) {
        fragmentTransitionManager.configureFragment(fragment, route)
        appNavigator.navigate(route, fragment, sharedElement)
    }
}
