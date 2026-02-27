package com.louis.app.cavity.ui.navigation

import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.louis.app.cavity.R

class Navigator {
    val transitionSelector: TransitionSelector = MaterialTransitionSelector()
    private val appNavigator: AppNavigator by lazy {
        NavComponentNavigator(
            resolvers = listOf(
                NavComponentHomeRouteResolver(),
                NavComponentWineOptionsRouteResolver()
            )
        )
    }

    fun navigate(route: AppRoute, fragment: Fragment, sharedElement: View? = null) {
        transitionSelector.configureFragment(fragment, route)
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
                    transitionSelector.configureDestinationFragment(fragment)
                }
            }, false
        )
    }
}
