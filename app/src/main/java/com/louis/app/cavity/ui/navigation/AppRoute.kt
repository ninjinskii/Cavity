package com.louis.app.cavity.ui.navigation

import androidx.annotation.IdRes
import com.louis.app.cavity.R
import com.louis.app.cavity.model.Wine

sealed interface AppRoute {
    val transition: TransitionSpec
        get() = TransitionSpec.FadeThrough

    val destinationTransition: TransitionSpec
        get() = transition
}

sealed interface GlobalRoute : AppRoute {
    data class To(@param:IdRes val destinationId: Int) : GlobalRoute
}

sealed interface HomeRoute : AppRoute {
    data class AddWine(val countyId: Long) : HomeRoute {
        override val transition = TransitionSpec.SharedAxis(Axis.Z)
    }

    data class BottleDetails(val wineId: Long) : HomeRoute {
        override val transition = TransitionSpec.ElevationScale
        override val destinationTransition = TransitionSpec.ContainerTransform(null)
    }

    data class AddBottle(val wineId: Long) : HomeRoute {
        override val transition = TransitionSpec.SharedAxis(Axis.Z)
    }

    data class WineOptions(val wine: Wine) : HomeRoute {
        override val transition = TransitionSpec.None
    }
}

sealed interface WineOptionsRoute : AppRoute {
    override val transition: TransitionSpec
        get() = TransitionSpec.SharedAxis(Axis.Z)

    data class AddBottle(val wineId: Long) : WineOptionsRoute
    data class EditWine(val wineId: Long, val countyId: Long) : WineOptionsRoute
    data class ShowWineHistory(val wineId: Long) : WineOptionsRoute
}

sealed interface BottleDetailsRoute : AppRoute {
}

sealed interface SearchRoute : AppRoute {
    data class BottleDetails(val wineId: Long, val bottleId: Long) : SearchRoute {
        override val transition = TransitionSpec.ElevationScale

        override var destinationTransition = TransitionSpec.ContainerTransform(
            startContainerColor = android.R.color.transparent,
            endContainerColor = com.google.android.material.R.attr.colorSurface,
            startElevation = R.dimen.container_drop_elevation,
            endElevation = R.dimen.app_bar_elevation
        )
    }
}

sealed interface AccountRoute : AppRoute {
    data object Login : AccountRoute
    data object Account : AccountRoute
}
