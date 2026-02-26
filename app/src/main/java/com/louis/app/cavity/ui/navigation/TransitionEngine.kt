package com.louis.app.cavity.ui.navigation

import androidx.fragment.app.Fragment
import com.louis.app.cavity.util.L

sealed interface TransitionSpec {
    data object None : TransitionSpec
    data object FadeThrough : TransitionSpec
    data object ElevationScale : TransitionSpec
    data class SharedAxis(val axis: Int) : TransitionSpec
    data class ContainerTransform(val color: Pair<Int, Int>, val elevation: Pair<Float, Float>) :
        TransitionSpec
}

interface TransitionEngine {
    fun apply(fromFragment: Fragment, toRoute: AppRoute)
}

class MaterialTransitionEngine : TransitionEngine {
    override fun apply(fromFragment: Fragment, toRoute: AppRoute) {
        val transitionHelper = TransitionHelper(fromFragment)
        L.v("Transitioning from fragment: $fromFragment")

        when (toRoute.transition) {
            TransitionSpec.None -> Unit
            TransitionSpec.FadeThrough -> transitionHelper.setFadeThroughOnEnterAndExit()
            TransitionSpec.ElevationScale -> transitionHelper.setElevationScale()
            is TransitionSpec.ContainerTransform -> handleContainerTransform(
                toRoute.transition,
                transitionHelper
            )

            is TransitionSpec.SharedAxis -> transitionHelper.setSharedAxisTransition(
                (toRoute.transition as TransitionSpec.SharedAxis).axis, true
            )
        }
    }

    private fun handleContainerTransform(spec: TransitionSpec, transitionHelper: TransitionHelper) {
        if (spec !is TransitionSpec.ContainerTransform) {
            return
        }

        val options = spec.run {
            TransitionHelper.ContainerTransformOptions(
                color.first, color.second, elevation.first, elevation.second
            )
        }

        transitionHelper.setContainerTransformTransition(options, true)
    }
}
