package com.louis.app.cavity.ui.navigation

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.model.Wine
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

sealed interface AppRoute {
    val transition: TransitionSpec
        get() = TransitionSpec.FadeThrough
}

sealed interface HomeRoute : AppRoute {
    data object Settings : HomeRoute
    data object Search : HomeRoute
    data object Manager : HomeRoute
    data object History : HomeRoute
    data object Stats : HomeRoute
    data object Tasting : HomeRoute
    data class AddWine(val countyId: Long) : HomeRoute {
        override val transition = TransitionSpec.SharedAxis(MaterialSharedAxis.Z)
    }

    data class BottleDetails(val wineId: Long, val sharedElement: Pair<View?, String>) : HomeRoute {
        override val transition = TransitionSpec.ElevationScale
    }

    data class AddBottle(val wineId: Long) : HomeRoute {
        override val transition = TransitionSpec.SharedAxis(MaterialSharedAxis.Z)
    }

    data class WineOptions(val wine: Wine) : HomeRoute {
        override val transition = TransitionSpec.None
    }
}

sealed interface WineOptionsRoute : AppRoute {
    override val transition: TransitionSpec
        get() = TransitionSpec.SharedAxis(MaterialSharedAxis.Z)

    data class AddBottle(val wineId: Long) : WineOptionsRoute
    data class EditWine(val wineId: Long, val countyId: Long) : WineOptionsRoute
    data class ShowWineHistory(val wineId: Long) : WineOptionsRoute
}

sealed interface AccountRoute : AppRoute {
    data object Login : AccountRoute
    data object Account : AccountRoute
}

abstract class TypedRouteResolver<T : AppRoute>(
    private val type: KClass<T>
) : RouteResolver {

    override fun canHandle(route: AppRoute) = type.isInstance(route)

    override fun resolve(route: AppRoute, fragment: Fragment) {
        val casted = type.safeCast(route) ?: error("Bad type")
        resolveTyped(casted, fragment)
    }

    protected abstract fun resolveTyped(route: T, fragment: Fragment)
}
