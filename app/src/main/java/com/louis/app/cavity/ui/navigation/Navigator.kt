package com.louis.app.cavity.ui.navigation

import android.content.Context
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.louis.app.cavity.R

class Navigator {
    val menus = mutableListOf<Menu>()
    val transitionExecutor: TransitionExecutor = MaterialTransitionExecutor()
    private val appNavigator: AppNavigator by lazy {
        NavComponentNavigator(
            resolvers = listOf(
                NavComponentGlobalRouteResolver(),
                NavComponentHomeRouteResolver(),
                NavComponentWineOptionsRouteResolver(),
                NavComponentSearchRouteResolver()
            )
        )
    }

    fun navigate(route: AppRoute, fragment: Fragment, sharedElement: View? = null) {
        transitionExecutor.configureFragment(fragment, route)
        appNavigator.navigate(route, fragment, sharedElement)
    }

    fun navigateUp(fragment: Fragment) {
        appNavigator.navigateUp(fragment)
    }

    fun popBackStack(fragment: Fragment) {
        appNavigator.popBackStack(fragment)
    }

    fun setup(activity: AppCompatActivity) {
        val navHost = activity.supportFragmentManager
            .findFragmentById(R.id.navHostFragment)

        navHost?.childFragmentManager?.registerFragmentLifecycleCallbacks(
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
                        menus.forEach { it.forEach { item ->  item.isChecked = false } }
                    }
                }
            }, false
        )
    }

    fun syncMenu(vararg menu: Menu) {
        this.menus.addAll(menu)
    }
}
