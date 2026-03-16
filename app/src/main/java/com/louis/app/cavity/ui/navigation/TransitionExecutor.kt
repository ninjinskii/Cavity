package com.louis.app.cavity.ui.navigation

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface TransitionSpec : Parcelable {
    @Parcelize
    data object None : TransitionSpec, Parcelable

    @Parcelize
    data object FadeThrough : TransitionSpec, Parcelable

    @Parcelize
    data object ElevationScale : TransitionSpec, Parcelable

    @Parcelize
    data class SharedAxis(val axis: Axis) : TransitionSpec, Parcelable
}

@Parcelize
sealed interface SharedElementTransitionSpec : Parcelable {
    @Parcelize
    data object None : SharedElementTransitionSpec, Parcelable

    @Parcelize
    data class ContainerTransform(
        @param:AttrRes @param:ColorRes val startContainerColor: Int? = null,
        @param:AttrRes @param:ColorRes val endContainerColor: Int? = null,
        @param:DimenRes val startElevation: Int? = null,
        @param:DimenRes val endElevation: Int? = null
    ) :
        SharedElementTransitionSpec, Parcelable
}

enum class Axis {
    X, Y, Z
}

interface TransitionExecutor {
    var pendingDestinationTransition: TransitionSpec
    var pendingSharedElementDestinationTransition: SharedElementTransitionSpec
    fun configureFragment(source: Fragment, toRoute: AppRoute)
    fun restoreFragment(
        fragment: Fragment,
        navigatingForward: Boolean,
        spec: TransitionSpec,
        sharedSpec: SharedElementTransitionSpec? = null
    )

    fun configureDestinationFragment(destination: Fragment)
    fun restoreDestinationFragment(
        destination: Fragment,
        spec: TransitionSpec,
        sharedSpec: SharedElementTransitionSpec? = null
    )

    fun restoreState(fragment: Fragment)
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

        source.arguments = (source.arguments ?: Bundle()).apply {
            putParcelable("transition-out", sourceTransition)
        }

   /*     restoreFragment(
            source,
            pendingDestinationTransition,
            pendingSharedElementDestinationTransition
        )*/

        when (sourceTransition) {
            TransitionSpec.None -> Unit
            TransitionSpec.FadeThrough -> transitionHelper.setFadeThroughOnEnterAndExit()
            TransitionSpec.ElevationScale -> transitionHelper.setElevationScale()
            is TransitionSpec.SharedAxis -> {
                val axis = toMaterialSharedAxis(sourceTransition.axis)
                transitionHelper.setSharedAxisTransition(axis, navigatingForward = true)
            }
        }
    }

    override fun configureDestinationFragment(destination: Fragment) {
        val transitionHelper = TransitionHelper(destination)
        val destinationTransition = pendingDestinationTransition
        val destinationSharedElementTransition = pendingSharedElementDestinationTransition

        destination.arguments = (destination.arguments ?: Bundle()).apply {
            putParcelable("transition-in", destinationTransition)
            putParcelable("shared-transition-in", destinationSharedElementTransition)
        }

     /*   restoreFragment(
            destination,
            pendingDestinationTransition,
            pendingSharedElementDestinationTransition
        )*/

        when (destinationTransition) {
            TransitionSpec.None -> Unit
            TransitionSpec.FadeThrough -> transitionHelper.setFadeThrough(navigatingForward = false)
            TransitionSpec.ElevationScale -> Unit
            is TransitionSpec.SharedAxis -> {
                val axis = toMaterialSharedAxis(destinationTransition.axis)
                transitionHelper.setSharedAxisTransition(axis, navigatingForward = false)
            }
        }

        when (destinationSharedElementTransition) {
            is SharedElementTransitionSpec.ContainerTransform ->
                transitionHelper.setContainerTransformTransition(
                    destinationSharedElementTransition,
                    navigatingForward = true
                )

            else -> Unit
        }
    }

    override fun restoreFragment(
        fragment: Fragment,
        navigatingForward: Boolean,
        spec: TransitionSpec,
        sharedSpec: SharedElementTransitionSpec?
    ) {
        val transitionHelper = TransitionHelper(fragment)

        when (spec) {
            TransitionSpec.None -> Unit
            TransitionSpec.FadeThrough -> transitionHelper.setFadeThroughOnEnterAndExit()
            TransitionSpec.ElevationScale -> transitionHelper.setElevationScale()
            is TransitionSpec.SharedAxis -> {
                val axis = toMaterialSharedAxis(spec.axis)
                transitionHelper.setSharedAxisTransition(axis, navigatingForward = true)
            }
        }

        when (sharedSpec) {
            is SharedElementTransitionSpec.ContainerTransform ->
                transitionHelper.setContainerTransformTransition(
                    sharedSpec,
                    navigatingForward = false
                )

            else -> Unit
        }
    }


    override fun restoreDestinationFragment(
        destination: Fragment,
        spec: TransitionSpec,
        sharedSpec: SharedElementTransitionSpec?
    ) {
        val transitionHelper = TransitionHelper(destination)

        when (spec) {
            TransitionSpec.None -> Unit
            TransitionSpec.FadeThrough -> transitionHelper.setFadeThrough(false)
            TransitionSpec.ElevationScale -> Unit
            is TransitionSpec.SharedAxis -> {
                val axis = toMaterialSharedAxis(spec.axis)
                transitionHelper.setSharedAxisTransition(axis, false)
            }
        }

        when (sharedSpec) {
            is SharedElementTransitionSpec.ContainerTransform ->
                transitionHelper.setContainerTransformTransition(
                    sharedSpec,
                    navigatingForward = true
                )

            else -> Unit
        }
    }

    override fun restoreState(fragment: Fragment) {
        fragment.arguments
        fragment.arguments?.run {
            getParcelable<TransitionSpec>("transition-in")?.let {
                val shared = getParcelable<SharedElementTransitionSpec>("shared-transition-in")
                restoreDestinationFragment(fragment, it, shared)
            }

            getParcelable<TransitionSpec>("transition-out")?.let {
                restoreFragment(fragment, false, it)
            }
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
