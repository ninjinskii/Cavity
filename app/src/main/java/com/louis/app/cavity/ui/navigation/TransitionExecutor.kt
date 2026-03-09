package com.louis.app.cavity.ui.navigation

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.domain.error.SentryErrorReporter
import com.louis.app.cavity.util.requireThemeColor
import java.lang.IllegalArgumentException

sealed interface TransitionSpec {
    data object None : TransitionSpec
    data object FadeThrough : TransitionSpec
    data object ElevationScale : TransitionSpec
    data class SharedAxis(val axis: Axis) : TransitionSpec
}

sealed interface SharedElementTransitionSpec {
    data object None : SharedElementTransitionSpec
    data class ContainerTransform(
        @param:AttrRes @param:ColorRes val startContainerColor: Int? = null,
        @param:AttrRes @param:ColorRes val endContainerColor: Int? = null,
        @param:DimenRes val startElevation: Int? = null,
        @param:DimenRes val endElevation: Int? = null
    ) :
        SharedElementTransitionSpec
}

enum class Axis {
    X, Y, Z
}

interface TransitionExecutor {
    var pendingDestinationTransition: TransitionSpec
    var pendingSharedElementDestinationTransition: SharedElementTransitionSpec
    fun configureFragment(source: Fragment, toRoute: AppRoute)
    fun configureDestinationFragment(destination: Fragment)
}

class MaterialTransitionExecutor : TransitionExecutor {
    override var pendingDestinationTransition: TransitionSpec = TransitionSpec.None
    override var pendingSharedElementDestinationTransition: SharedElementTransitionSpec =
        SharedElementTransitionSpec.None

    override fun configureFragment(source: Fragment, toRoute: AppRoute) {
        pendingDestinationTransition = toRoute.destinationTransition
        pendingSharedElementDestinationTransition = toRoute.sharedElementTransition
        val transitionHelper = TransitionHelper(source)
        val sourceTransition = toRoute.transition

        when (sourceTransition) {
            TransitionSpec.None -> Unit
            TransitionSpec.FadeThrough -> transitionHelper.setFadeThroughOnEnterAndExit()
            TransitionSpec.ElevationScale -> transitionHelper.setElevationScale()
            is TransitionSpec.SharedAxis -> {
                val axis = toMaterialSharedAxis(sourceTransition.axis)
                transitionHelper.setSharedAxisTransition(axis, true)
            }
        }
    }

    override fun configureDestinationFragment(destination: Fragment) {
        val transitionHelper = TransitionHelper(destination)
        val destinationTransition = pendingDestinationTransition
        val destinationSharedElementTransition = pendingSharedElementDestinationTransition

        when (destinationTransition) {
            TransitionSpec.None -> Unit
            TransitionSpec.FadeThrough -> transitionHelper.setFadeThrough(false)
            TransitionSpec.ElevationScale -> Unit
            is TransitionSpec.SharedAxis -> {
                val axis = toMaterialSharedAxis(destinationTransition.axis)
                transitionHelper.setSharedAxisTransition(axis, false)
            }
        }

        when (destinationSharedElementTransition) {
            is SharedElementTransitionSpec.ContainerTransform ->
                transitionHelper.setContainerTransformTransition(
                    destinationSharedElementTransition,
                    false
                )

            else -> Unit
        }
    }

    private fun toMaterialSharedAxis(axis: Axis): Int {
        return when (axis) {
            Axis.X -> MaterialSharedAxis.X
            Axis.Y -> MaterialSharedAxis.Y
            Axis.Z -> MaterialSharedAxis.Z
        }
    }
}
