package com.louis.app.cavity.ui.navigation

import androidx.fragment.app.Fragment
import com.louis.app.cavity.util.L

sealed interface TransitionSpec {
    data object None : TransitionSpec
    data object FadeThrough : TransitionSpec
    data object ElevationScale : TransitionSpec
    data class SharedAxis(val axis: Int) : TransitionSpec
    data class ContainerTransform(val options: TransitionHelper.ContainerTransformOptions?) :
        TransitionSpec
}

interface TransitionSelector {
    var pendingDestinationTransition: TransitionSpec
    fun configureFragment(source: Fragment, toRoute: AppRoute)
    fun configureDestinationFragment(destination: Fragment)
}

class MaterialTransitionSelector : TransitionSelector {
    override var pendingDestinationTransition: TransitionSpec = TransitionSpec.None

    override fun configureFragment(source: Fragment, toRoute: AppRoute) {
        pendingDestinationTransition = toRoute.destinationTransition
        val transitionHelper = TransitionHelper(source)
        L.v("Transitioning from fragment: $source")

        when (toRoute.transition) {
            TransitionSpec.None -> Unit
            TransitionSpec.FadeThrough -> transitionHelper.setFadeThroughOnEnterAndExit()
            TransitionSpec.ElevationScale -> transitionHelper.setElevationScale()
            is TransitionSpec.ContainerTransform -> handleContainerTransform(
                toRoute.transition,
                transitionHelper,
                navigatingForward = true
            )

            is TransitionSpec.SharedAxis -> transitionHelper.setSharedAxisTransition(
                (toRoute.transition as TransitionSpec.SharedAxis).axis, true
            )
        }
    }

    override fun configureDestinationFragment(destination: Fragment) {
        val transitionHelper = TransitionHelper(destination)

        when (pendingDestinationTransition) {
            TransitionSpec.None -> Unit
            TransitionSpec.FadeThrough -> transitionHelper.setFadeThrough(false)
            TransitionSpec.ElevationScale -> Unit
            is TransitionSpec.ContainerTransform -> handleContainerTransform(
                pendingDestinationTransition,
                transitionHelper,
                navigatingForward = false,
            )

            is TransitionSpec.SharedAxis -> transitionHelper.setSharedAxisTransition(
                (pendingDestinationTransition as TransitionSpec.SharedAxis).axis, false
            )
        }
    }

    private fun handleContainerTransform(
        spec: TransitionSpec,
        transitionHelper: TransitionHelper,
        navigatingForward: Boolean
    ) {
        if (spec !is TransitionSpec.ContainerTransform) {
            return
        }

        transitionHelper.setContainerTransformTransition(spec.options, !navigatingForward)
    }
}
