package com.louis.app.cavity.ui.navigation

import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.ui.home.FragmentHome
import com.louis.app.cavity.ui.home.FragmentHomeDirections
import com.louis.app.cavity.ui.navigation.TransitionHelper
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

sealed interface AppRoute

sealed interface HomeRoute : AppRoute {
    data object Settings : HomeRoute
    data class AddWine(val countryId: Long) : HomeRoute
    data class BottleDetails(val wineId: Long, val bottleId: Long) : HomeRoute
}

sealed interface AccountRoute : AppRoute {
    data object Login : AccountRoute
    data object Account : AccountRoute
}

interface RouteResolver<T : AppRoute> {
    fun resolve(route: T, fragment: Fragment)
}

abstract class NavComponentRouteResolver<T : AppRoute>(
    private val type: KClass<T>
) : RouteResolver<T> {

    protected val Fragment.navController: NavController
        get() = findNavController()

    override fun resolve(route: T, fragment: Fragment) {
        resolve(route, fragment, fragment.navController)
    }

    protected abstract fun resolve(route: T, fragment: Fragment, navController: NavController)
    fun canHandle(route: AppRoute): Boolean = type.isInstance(route)
    fun cast(route: AppRoute): T? = type.safeCast(route)
}


class HomeRouteResolver : NavComponentRouteResolver<HomeRoute>(HomeRoute::class) {
    override fun resolve(route: HomeRoute, fragment: Fragment, navController: NavController) {
        when (route) {
            HomeRoute.Settings -> navController.navigate(R.id.settings_dest)
            is HomeRoute.AddWine -> navController.navigate(
                FragmentHomeDirections.homeToAddWine(route.countryId)
            )

            is HomeRoute.BottleDetails ->
                navController.navigate(
                    FragmentHomeDirections.homeToBottleDetails(
                        route.wineId,
                        route.bottleId
                    )
                )
        }
    }
}

class AppNavigator(
    private val resolvers: List<NavComponentRouteResolver<AppRoute>>
) {

    fun navigate(route: AppRoute, fragment: Fragment) {
        val resolver = resolvers.firstOrNull { it.canHandle(route) }
            ?: error("No resolver for $route")

        val castedRoute = resolver.cast(route)
            ?: error("Route cannot be casted to resolver type")

        resolver.resolve(castedRoute, fragment)
    }
}

object NavigatorProvider {
    val appNavigator: AppNavigator by lazy {
        AppNavigator(
            resolvers = listOf(
                HomeRouteResolver() as NavComponentRouteResolver<AppRoute>,
            )
        )
    }
}


/*open class HomeNavigationManager(fragment: FragmentHome) {
    private val transitionHelper = TransitionHelper(fragment)

    open fun navigateToAddWine(countyId: Long) {
        transitionHelper.setSharedAxisTransition(MaterialSharedAxis.Z, navigatingForward = true)
    }
}

class NavComponentHomeNavigationManager(private val fragment: FragmentHome) :
    HomeNavigationManager(fragment) {

    override fun navigateToAddWine(countyId: Long) {
        super.navigateToAddWine(countyId)
        FragmentHomeDirections.homeToAddWine(countyId).let {
            fragment.findNavController().navigate(it)
        }
    }
}*/
