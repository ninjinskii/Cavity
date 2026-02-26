package com.louis.app.cavity.ui.navigation

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

interface RouteResolver {
    fun canHandle(route: AppRoute): Boolean
    fun resolve(route: AppRoute, fragment: Fragment)
}

abstract class NavComponentRouteResolver<T : AppRoute>(type: KClass<T>) :
    TypedRouteResolver<T>(type) {

    protected val transitionEngine = MaterialTransitionEngine()

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

class NavComponentHomeRouteResolver : NavComponentRouteResolver<HomeRoute>(HomeRoute::class) {
    override fun resolveTyped(route: HomeRoute, fragment: Fragment) {
        transitionEngine.apply(fragment, route)

        when (route) {
            HomeRoute.Settings -> fragment.navigate(R.id.settings_dest)
            HomeRoute.Search -> fragment.navigate(FragmentHomeDirections.homeToSearch())
            HomeRoute.History -> fragment.navigate(FragmentHomeDirections.homeToHistory())
            HomeRoute.Manager -> fragment.navigate(FragmentHomeDirections.homeToManager())
            HomeRoute.Stats -> fragment.navigate(FragmentHomeDirections.homeToStats())
            HomeRoute.Tasting -> fragment.navigate(FragmentHomeDirections.homeToTasting())
            is HomeRoute.WineOptions -> with(route.wine) {
                fragment.navigate(
                    FragmentHomeDirections.homeToWineOptions(
                        id,
                        countyId,
                        name,
                        naming,
                        isOrganic.toBoolean(),
                        color
                    )
                )
            }

            is HomeRoute.AddWine -> fragment.navigate(
                FragmentHomeDirections.homeToAddWine(countyId = route.countyId)
            )

            is HomeRoute.BottleDetails -> {
                val (sharedElement, transition) = route.sharedElement
                val direction = FragmentHomeDirections.homeToBottleDetails(route.wineId, -1L)
                val extra = sharedElement?.let { FragmentNavigatorExtras(it to transition) }
                fragment.navigate(direction, extra)
            }

            is HomeRoute.AddBottle -> fragment.navigate(
                FragmentHomeDirections.homeToAddBottle(
                    route.wineId,
                    -1L
                )
            )
        }
    }
}

class NavComponentWineOptionsRouteResolver :
    NavComponentRouteResolver<WineOptionsRoute>(WineOptionsRoute::class) {

    override fun resolveTyped(route: WineOptionsRoute, fragment: Fragment) {
        transitionEngine.apply(fragment, route)

        when (route) {
            is WineOptionsRoute.AddBottle -> fragment.navigate(
                WineOptionsBottomSheetDirections.wineOptionsToAddBottle(
                    route.wineId
                )
            )

            is WineOptionsRoute.EditWine -> fragment.navigate(
                WineOptionsBottomSheetDirections.wineOptionsToEditWine(
                    route.wineId,
                    route.countyId
                )
            )

            is WineOptionsRoute.ShowWineHistory -> fragment.navigate(
                WineOptionsBottomSheetDirections.wineOptionsToHistory(
                    route.wineId
                )
            )
        }
    }
}

class NavComponentAccountRouteResolver :
    NavComponentRouteResolver<AccountRoute>(AccountRoute::class) {

    override fun resolveTyped(route: AccountRoute, fragment: Fragment) {
        transitionEngine.apply(fragment, route)

        when (route) {
            AccountRoute.Account -> fragment.navigate(R.id.settings_dest)
            AccountRoute.Login -> fragment.navigate(R.id.settings_dest)
        }
    }
}
