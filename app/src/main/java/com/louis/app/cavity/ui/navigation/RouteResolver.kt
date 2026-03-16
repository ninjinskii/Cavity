package com.louis.app.cavity.ui.navigation

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.ui.addwine.FragmentAddWineDirections
import com.louis.app.cavity.ui.bottle.FragmentBottleDetailsDirections
import com.louis.app.cavity.ui.home.FragmentHomeDirections
import com.louis.app.cavity.ui.home.WineOptionsBottomSheetDirections
import com.louis.app.cavity.ui.search.FragmentSearchDirections
import com.louis.app.cavity.ui.tasting.FragmentTastingsDirections
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

interface RouteResolver {
    fun canHandle(route: AppRoute): Boolean
    fun resolve(route: AppRoute, fragment: Fragment, sharedElement: View?)
}

abstract class TypedRouteResolver<T : AppRoute> : RouteResolver {
    abstract val type: KClass<T>

    override fun canHandle(route: AppRoute) = type.isInstance(route)

    override fun resolve(route: AppRoute, fragment: Fragment, sharedElement: View?) {
        val casted = type.safeCast(route) ?: error("Bad type")
        resolveTyped(casted, fragment, sharedElement)
    }

    protected abstract fun resolveTyped(route: T, fragment: Fragment, sharedElement: View?)
}

abstract class NavComponentRouteResolver<T : AppRoute> : TypedRouteResolver<T>() {
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

    protected fun Fragment.popUpTo(@IdRes destId: Int) {
        findNavController().apply {
            popBackStack(this.graph.findStartDestination().id, destId == R.id.home_dest)
            navigate(destId)
        }
    }
}

class NavComponentGlobalRouteResolver : NavComponentRouteResolver<GlobalRoute>() {
    override val type = GlobalRoute::class

    override fun resolveTyped(route: GlobalRoute, fragment: Fragment, sharedElement: View?) {
        val id = when (route) {
            is GlobalRoute.To -> route.destinationId
        }

        fragment.popUpTo(id)

//        fragment.navigate(id)
    }
}

class NavComponentHomeRouteResolver : NavComponentRouteResolver<HomeRoute>() {
    override val type = HomeRoute::class

    override fun resolveTyped(route: HomeRoute, fragment: Fragment, sharedElement: View?) {
        val direction: NavDirections = when (route) {
            is HomeRoute.WineOptions -> with(route.wine) {
                FragmentHomeDirections.homeToWineOptions(
                    id,
                    countyId,
                    route.storageLocationActive
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


class NavComponentWineOptionsRouteResolver : NavComponentRouteResolver<WineOptionsRoute>() {
    override val type = WineOptionsRoute::class

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

class NavComponentAddWineRouteResolver : NavComponentRouteResolver<AddWineRoute>() {
    override val type = AddWineRoute::class

    override fun resolveTyped(route: AddWineRoute, fragment: Fragment, sharedElement: View?) {
        val direction = when (route) {
            AddWineRoute.Camera -> FragmentAddWineDirections.addWineToCamera()
        }

        fragment.navigate(direction)
    }
}

class NavComponentBottleDetailsRouteResolver : NavComponentRouteResolver<BottleDetailsRoute>() {
    override val type = BottleDetailsRoute::class

    override fun resolveTyped(route: BottleDetailsRoute, fragment: Fragment, sharedElement: View?) {
        val direction = when (route) {
            is BottleDetailsRoute.AddBottle ->
                FragmentBottleDetailsDirections.bottleDetailsToEditBottle(route.wineId, -1)

            is BottleDetailsRoute.EditBottle ->
                FragmentBottleDetailsDirections.bottleDetailsToEditBottle(
                    route.wineId,
                    route.bottleId
                )

            is BottleDetailsRoute.ConsumeBottle ->
                FragmentBottleDetailsDirections.bottleDetailsToConsumeBottle(route.bottleId)

            is BottleDetailsRoute.GiveBottle ->
                FragmentBottleDetailsDirections.bottleDetailsToGiftBottle(route.bottleId)

            is BottleDetailsRoute.BottleHistory ->
                FragmentBottleDetailsDirections.bottleDetailsToHistory(-1)

            is BottleDetailsRoute.TastingLog -> FragmentBottleDetailsDirections.bottleDetailsToHistory(
                -1,
                route.wineId,
                true
            )
        }

        fragment.navigate(direction)
    }
}

class NavComponentSearchRouteResolver : NavComponentRouteResolver<SearchRoute>() {
    override val type = SearchRoute::class

    override fun resolveTyped(route: SearchRoute, fragment: Fragment, sharedElement: View?) {
        val direction = when (route) {
            is SearchRoute.BottleDetails -> FragmentSearchDirections.searchToBottleDetails(
                route.wineId,
                route.bottleId
            )
        }

        val extra = sharedElement?.let { FragmentNavigatorExtras(it to it.transitionName) }
        fragment.navigate(direction, extra)
    }
}

class NavComponentTastingRouteResolver : NavComponentRouteResolver<TastingRoute>() {
    override val type = TastingRoute::class

    override fun resolveTyped(route: TastingRoute, fragment: Fragment, sharedElement: View?) {
        val direction = when (route) {
            TastingRoute.AddTasting -> FragmentTastingsDirections.tastingToAddTasting()
            is TastingRoute.TastingDetails -> FragmentTastingsDirections.tastingToTastingOverview(
                route.tastingId,
                route.tastingOpportunity
            )
        }

        val extra = sharedElement?.let { FragmentNavigatorExtras(it to it.transitionName) }
        fragment.navigate(direction, extra)
    }
}
