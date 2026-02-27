package com.louis.app.cavity.ui.navigation

import android.graphics.Color
import android.view.View
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.util.themeColor

sealed interface AppRoute {
    val transition: TransitionSpec
        get() = TransitionSpec.FadeThrough

    val destinationTransition: TransitionSpec
        get() = transition
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

    data class BottleDetails(val wineId: Long) : HomeRoute {
        override val transition = TransitionSpec.ElevationScale
        override val destinationTransition = TransitionSpec.ContainerTransform(null)
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

sealed interface BottleDetailsRoute : AppRoute {
}

// J'ai plusieurs choix pour destinationTransition: mettre cette logique dans le fragment bottle details
// et override destinationTransition en var au lieu de val, déplacer dans le transition selector, ou laisser ici
// ATTENTION j epense qu'il y a un trou dans la raquette par rapoort aux options du returnSharedELemenTransition, voir BottleDetailsFramgent L:79
// il est possible que la solution soit de passer deux options dans la destinationtransiton, une pour l'aller une pour le retour
sealed interface SearchRoute : AppRoute {
    data class BottleDetails(
        val wineId: Long,
        val bottleId: Long,
        val sharedElement: Pair<View?, String>
    ) :
        SearchRoute {
        override val transition = TransitionSpec.ElevationScale
        override var destinationTransition = run {
            val context = sharedElement.first?.context
            val options = context?.let {
                TransitionHelper.ContainerTransformOptions(
                    startContainerColor = Color.TRANSPARENT,
                    endContainerColor = it.themeColor(com.google.android.material.R.attr.colorSurface),
                    startElevation = it.resources.getDimension(R.dimen.container_drop_elevation),
                    endElevation = it.resources.getDimension(R.dimen.app_bar_elevation)
                )
            }

            TransitionSpec.ContainerTransform(options)
        }
    }
}

sealed interface AccountRoute : AppRoute {
    data object Login : AccountRoute
    data object Account : AccountRoute
}
