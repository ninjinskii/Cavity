package com.louis.app.cavity.ui.navigation

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.Dimension
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.error.SentryErrorReporter
import com.louis.app.cavity.util.requireThemeColor
import com.louis.app.cavity.util.themeColor
import java.lang.IllegalArgumentException

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

    fun setContainerTransformTransition(
        options: SharedElementTransitionSpec.ContainerTransform,
        navigatingForward: Boolean
    ) {
        val options = resolveAttrs(fragment.requireContext(), options)
        val transition = getContainerTransform().apply {
            duration = period
            drawingViewId = R.id.navHostFragment

            options.let {
                startContainerColor = it.startContainerColor
                endContainerColor = it.endContainerColor
                startElevation = it.startElevation
                endElevation = it.endElevation
            }
        }

        if (navigatingForward) {
            fragment.sharedElementEnterTransition = transition
        } else {
            fragment.sharedElementReturnTransition = transition
        }
    }

    fun setContainerTransformTransition(options: ContainerTransformOptions?, enter: Boolean) {
        val transition = getContainerTransform().apply {
            duration = period
            drawingViewId = R.id.navHostFragment

            options?.let {
                startContainerColor = it.startContainerColor
                endContainerColor = it.endContainerColor
                startElevation = it.startElevation
                endElevation = it.endElevation
            }
                ?: setAllContainerColors(resolveColor(com.google.android.material.R.attr.colorSurface))
        }

        if (enter) {
            fragment.sharedElementEnterTransition = transition
        } else {
            fragment.sharedElementReturnTransition = transition
        }
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
    }

    private fun resolveColor(@AttrRes color: Int) = fragment.requireContext().themeColor(color)

    private fun resolveAttrs(
        context: Context,
        spec: SharedElementTransitionSpec.ContainerTransform
    ): ContainerTransformOptions {
        val defaultColor = com.google.android.material.R.attr.colorSurface
        val defaultElevation = 0f
        val startColor = resolveColorOrAttr(context, spec.startContainerColor ?: defaultColor)
        val endColor = resolveColorOrAttr(context, spec.endContainerColor ?: defaultColor)
        val startElevation =
            spec.startElevation?.let { context.resources.getDimension(it) } ?: defaultElevation
        val endElevation =
            spec.endElevation?.let { context.resources.getDimension(it) } ?: defaultElevation

        return ContainerTransformOptions(startColor, endColor, startElevation, endElevation)
    }

    private fun resolveColorOrAttr(context: Context, @AttrRes @ColorRes resId: Int): Int {
        return try {
            // First, try to resolve as a color
            context.getColor(resId)
        } catch (_: Resources.NotFoundException) {
            try {
                // Then, try to resolve as a color attribute
                context.requireThemeColor(resId)
            } catch (e: IllegalArgumentException) {
                val errorReporter = SentryErrorReporter.getInstance(context)
                errorReporter.captureMessage(
                    "Failed to resolve color or attr $resId for material container transform. Defaulting to Color.TRANSPARENT"
                )
                errorReporter.captureException(e)

                // Last, default to transparent
                return Color.TRANSPARENT
            }
        }
    }

    data class ContainerTransformOptions(
        @param:ColorInt val startContainerColor: Int,
        @param:ColorInt val endContainerColor: Int,
        @param:Dimension val startElevation: Float = 0F,
        @param:Dimension val endElevation: Float = 0F
    )
}
