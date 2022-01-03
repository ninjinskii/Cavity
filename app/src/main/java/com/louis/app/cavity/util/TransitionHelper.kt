package com.louis.app.cavity.util

import android.graphics.Color
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R

class TransitionHelper(private val fragment: Fragment) {
    private val period = fragment.resources.getInteger(R.integer.cavity_motion_long).toLong()

    fun setSharedAxisTransition(axis: Int, navigatingForward: Boolean) {
        if (navigatingForward) {
            fragment.apply {
                exitTransition = getSharedAxis(axis, true)
                reenterTransition = getSharedAxis(axis, false)
            }
        } else {
            fragment.apply {
                enterTransition = getSharedAxis(axis, true)
                returnTransition = getSharedAxis(axis, false)
            }
        }
    }

    fun setContainerTransformTransition() {
        fragment.sharedElementEnterTransition = getContainerTransform()
    }

    fun setFadeThrough(navigatingForward: Boolean) {
        if (navigatingForward) {
            fragment.apply {
                exitTransition = getFadeThrough()
                reenterTransition = getFadeThrough()
            }
        } else {
            fragment.apply {
                enterTransition = getFadeThrough()
                returnTransition = getFadeThrough()
            }
        }
    }

    fun setFadeThroughOnEnterAndExit() {
        fragment.apply {
            enterTransition = getFadeThrough()
            exitTransition = getFadeThrough()
        }
    }

    fun setElevationScale() {
        fragment.apply {
            exitTransition = MaterialElevationScale(true)
            reenterTransition = MaterialElevationScale(false)
        }
    }

    private fun getFadeThrough() = MaterialFadeThrough().apply {
        duration = period
        excludeTarget(R.id.appBar, true)
    }

    private fun getSharedAxis(axis: Int, forward: Boolean) =
        MaterialSharedAxis(axis, forward).apply {
            duration = period
            excludeTarget(R.id.appBar, true)
        }

    private fun getContainerTransform() = MaterialContainerTransform().apply {
        duration = period //500
        scrimColor = Color.TRANSPARENT
        drawingViewId = R.id.navHostFragment
        endContainerColor = fragment.requireContext().themeColor(R.attr.colorSurface)
        setAllContainerColors(fragment.requireContext().themeColor(R.attr.colorSurface))
    }
}
