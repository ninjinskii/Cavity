package com.louis.app.cavity.ui.navigation

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.ui.home.FragmentHomeDirections
import com.louis.app.cavity.ui.home.WineOptionsBottomSheetDirections
import com.louis.app.cavity.util.toBoolean
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

interface RouteResolver {
    fun canHandle(route: AppRoute): Boolean
    fun resolve(route: AppRoute, fragment: Fragment, sharedElement: View?)
}

abstract class TypedRouteResolver<T : AppRoute>(
    private val type: KClass<T>
) :
    RouteResolver {

    override fun canHandle(route: AppRoute) = type.isInstance(route)

    override fun resolve(route: AppRoute, fragment: Fragment, sharedElement: View?) {
        val casted = type.safeCast(route) ?: error("Bad type")
        resolveTyped(casted, fragment, sharedElement)
    }

    protected abstract fun resolveTyped(route: T, fragment: Fragment, sharedElement: View?)
}

abstract class NavComponentRouteResolver<T : AppRoute>(
    type: KClass<T>
) :
    TypedRouteResolver<T>(type) {

    protected fun Fragment.navigate(navDirections: NavDirections) {
        findNavController().navigate(navDirections)
    }

    protected fun Fragment.navigate(navDirections: NavDirections, extra: Navigator.Extras?) {
        if (extra == null) {
            this.navigate(navDirections)
            return
        }

        findNavController().navigate(navDirections, extra)
    }

    protected fun Fragment.navigate(@IdRes destId: Int) {
        findNavController().navigate(destId)
    }
}

class NavComponentHomeRouteResolver() :
    NavComponentRouteResolver<HomeRoute>(HomeRoute::class) {

    override fun resolveTyped(route: HomeRoute, fragment: Fragment, sharedElement: View?) {
        val direction: NavDirections = when (route) {
            HomeRoute.Settings -> FragmentHomeDirections.homeToSettings()
            HomeRoute.Search -> FragmentHomeDirections.homeToSearch()
            HomeRoute.History -> FragmentHomeDirections.homeToHistory()
            HomeRoute.Manager -> FragmentHomeDirections.homeToManager()
            HomeRoute.Stats -> FragmentHomeDirections.homeToStats()
            HomeRoute.Tasting -> FragmentHomeDirections.homeToTasting()
            is HomeRoute.WineOptions -> with(route.wine) {
                FragmentHomeDirections.homeToWineOptions(
                    id,
                    countyId,
                    name,
                    naming,
                    isOrganic.toBoolean(),
                    color
                )
            }

            is HomeRoute.AddWine -> FragmentHomeDirections.homeToAddWine(countyId = route.countyId)
            is HomeRoute.AddBottle -> FragmentHomeDirections.homeToAddBottle(route.wineId, -1L)
            is HomeRoute.BottleDetails -> FragmentHomeDirections.homeToBottleDetails(
                route.wineId,
                -1L
            )
        }

        val extra = sharedElement?.let { FragmentNavigatorExtras(it to it.transitionName) }
        fragment.navigate(direction, extra)
    }
}


class NavComponentWineOptionsRouteResolver() :
    NavComponentRouteResolver<WineOptionsRoute>(WineOptionsRoute::class) {

    override fun resolveTyped(route: WineOptionsRoute, fragment: Fragment, sharedElement: View?) {
        val direction = when (route) {
            is WineOptionsRoute.AddBottle ->
                WineOptionsBottomSheetDirections.wineOptionsToAddBottle(route.wineId)

            is WineOptionsRoute.EditWine ->
                WineOptionsBottomSheetDirections.wineOptionsToEditWine(route.wineId, route.countyId)

            is WineOptionsRoute.ShowWineHistory ->
                WineOptionsBottomSheetDirections.wineOptionsToHistory(route.wineId)
        }

        fragment.navigate(direction)
    }
}

class NavComponentBottleDetailsRouteResolver() :
    NavComponentRouteResolver<BottleDetailsRoute>(BottleDetailsRoute::class) {

    override fun resolveTyped(route: BottleDetailsRoute, fragment: Fragment, sharedElement: View?) {
    }
}

class NavComponentAccountRouteResolver() :
    NavComponentRouteResolver<AccountRoute>(AccountRoute::class) {

    override fun resolveTyped(route: AccountRoute, fragment: Fragment, sharedElement: View?) {
        when (route) {
            AccountRoute.Account -> fragment.navigate(R.id.settings_dest)
            AccountRoute.Login -> fragment.navigate(R.id.settings_dest)
        }
    }
}
