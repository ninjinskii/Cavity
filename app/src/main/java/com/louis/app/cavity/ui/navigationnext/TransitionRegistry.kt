package com.louis.app.cavity.ui.navigationnext

import com.louis.app.cavity.R
import com.louis.app.cavity.ui.navigation.transition.Axis
import com.louis.app.cavity.ui.navigation.transition.SharedElementTransitionSpec
import com.louis.app.cavity.ui.navigation.transition.TransitionSpec

data class NavTransitionAnimations(val spec: TransitionSpec, val sharedSpec: SharedElementTransitionSpec? = null)

object TransitionRegistry {
    val a = mapOf<Int, NavTransitionAnimations>(
        R.id.homeToSettings to NavTransitionAnimations(TransitionSpec.FadeThrough),
        R.id.homeToAddWine to NavTransitionAnimations(TransitionSpec.SharedAxis(Axis.Z)),
        R.id.homeToSearch to NavTransitionAnimations(TransitionSpec.FadeThrough),
        R.id.homeToBottleDetails to NavTransitionAnimations(
            TransitionSpec.ElevationScale,
            SharedElementTransitionSpec.ContainerTransform()
        ),
    )
}
