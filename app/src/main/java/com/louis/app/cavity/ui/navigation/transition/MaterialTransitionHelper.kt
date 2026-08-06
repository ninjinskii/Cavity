package com.louis.app.cavity.ui.navigation.transition

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

class MaterialTransitionHelper(private val fragment: Fragment) {
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
        val resolvedOptions = resolveAttrs(fragment.requireContext(), options)
        val transition = getContainerTransform().apply {
            duration = period
            drawingViewId = R.id.navHostFragment

            resolvedOptions.let {
                // Reversing because it makes more sense from AppRoute config point of view
                startContainerColor = it.endContainerColor
                endContainerColor = it.startContainerColor
                startElevation = it.endElevation
                endElevation = it.startElevation
            }
        }

        if (navigatingForward) {
            fragment.sharedElementEnterTransition = transition
            fragment.sharedElementReturnTransition = transition
        } else {
            throw IllegalArgumentException("No use case for back container transform until now")
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
        duration = period
        scrimColor = Color.TRANSPARENT
        drawingViewId = R.id.coordinator
    }

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
                ErrorReporterFactory.create(context).run {
                    captureMessage(
                        "Failed to resolve color or attr $resId for material container" +
                                " transform. Defaulting to Color.TRANSPARENT"
                    )
                    captureException(e)
                }

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
