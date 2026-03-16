package com.louis.app.cavity.ui.navigation.transition

import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis

class MaterialFragmentTransitionManager : FragmentArgumentsTransitionManager() {
    override fun applyTransition(
        fragment: Fragment,
        spec: TransitionSpec,
        navigatingForward: Boolean
    ) {
        val helper = MaterialTransitionHelper(fragment)

        when (spec) {
            TransitionSpec.None -> Unit
            TransitionSpec.FadeThrough ->
                if (navigatingForward) helper.setFadeThroughOnEnterAndExit()
                else helper.setFadeThrough(false)

            TransitionSpec.ElevationScale ->
                if (navigatingForward) {
                    helper.setElevationScale()
                }

            is TransitionSpec.SharedAxis -> {
                val axis = toMaterialSharedAxis(spec.axis)
                helper.setSharedAxisTransition(axis, navigatingForward)
            }
        }
    }

    override fun applySharedTransition(fragment: Fragment, spec: SharedElementTransitionSpec) {
        val helper = MaterialTransitionHelper(fragment)

        when (spec) {
            SharedElementTransitionSpec.None -> Unit
            is SharedElementTransitionSpec.ContainerTransform ->
                helper.setContainerTransformTransition(spec, navigatingForward = true)
        }
    }

    private fun toMaterialSharedAxis(axis: Axis): Int =
        when (axis) {
            Axis.X -> MaterialSharedAxis.X
            Axis.Y -> MaterialSharedAxis.Y
            Axis.Z -> MaterialSharedAxis.Z
        }
}
