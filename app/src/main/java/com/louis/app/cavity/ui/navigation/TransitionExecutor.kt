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
    data class ContainerTransform(
        @param:AttrRes @param:ColorRes val startContainerColor: Int? = null,
        @param:AttrRes @param:ColorRes val endContainerColor: Int? = null,
        @param:DimenRes val startElevation: Int? = null,
        @param:DimenRes val endElevation: Int? = null
    ) :
        TransitionSpec
}

enum class Axis {
    X, Y, Z
}

interface TransitionExecutor {
    var pendingDestinationTransition: TransitionSpec
    fun configureFragment(source: Fragment, toRoute: AppRoute)
    fun configureDestinationFragment(destination: Fragment)
}

class MaterialTransitionExecutor : TransitionExecutor {
    override var pendingDestinationTransition: TransitionSpec = TransitionSpec.None

    override fun configureFragment(source: Fragment, toRoute: AppRoute) {
        pendingDestinationTransition = toRoute.destinationTransition
        val transitionHelper = TransitionHelper(source)
        val sourceTransition = toRoute.transition

        when (sourceTransition) {
            TransitionSpec.None -> Unit
            TransitionSpec.FadeThrough -> transitionHelper.setFadeThroughOnEnterAndExit()
            TransitionSpec.ElevationScale -> transitionHelper.setElevationScale()
            is TransitionSpec.ContainerTransform -> handleContainerTransform(
                fragment = source,
                spec = toRoute.transition,
                transitionHelper,
                navigatingForward = true
            )

            is TransitionSpec.SharedAxis -> {
                val axis = toMaterialSharedAxis(sourceTransition.axis)
                transitionHelper.setSharedAxisTransition(axis, true)
            }
        }
    }

    override fun configureDestinationFragment(destination: Fragment) {
        val transitionHelper = TransitionHelper(destination)
        val destinationTransition = pendingDestinationTransition

        when (destinationTransition) {
            TransitionSpec.None -> Unit
            TransitionSpec.FadeThrough -> transitionHelper.setFadeThrough(false)
            TransitionSpec.ElevationScale -> Unit
            is TransitionSpec.ContainerTransform -> handleContainerTransform(
                fragment = destination,
                spec = destinationTransition,
                transitionHelper,
                navigatingForward = false
            )

            is TransitionSpec.SharedAxis -> {
                val axis = toMaterialSharedAxis(destinationTransition.axis)
                transitionHelper.setSharedAxisTransition(axis, false)
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

    private fun handleContainerTransform(
        fragment: Fragment,
        spec: TransitionSpec,
        transitionHelper: TransitionHelper,
        navigatingForward: Boolean
    ) {
        val context = fragment.context

        if (spec !is TransitionSpec.ContainerTransform || context == null) {
            return
        }

        val options = resolveAttrs(context, spec)
        transitionHelper.setContainerTransformTransition(options, !navigatingForward)
    }

    // Peut etre déplacer ça dans trnasition helper, peut etre aussi handleCOntainerTRansform
    // VOi aussi pour sépérarer dans un autre fichier les impélmentations concrètes, qui importent leur dep (material) dans le même fichier que l'interface pure
    private fun resolveAttrs(
        context: Context,
        spec: TransitionSpec.ContainerTransform
    ): TransitionHelper.ContainerTransformOptions {
        val defaultColor = com.google.android.material.R.attr.colorSurface
        val defaultElevation = 0f
        val startColor = resolveColorOrAttr(context, spec.startContainerColor ?: defaultColor)
        val endColor = resolveColorOrAttr(context, spec.endContainerColor ?: defaultColor)
        val startElevation =
            spec.startElevation?.let { context.resources.getDimension(it) } ?: defaultElevation
        val endElevation =
            spec.endElevation?.let { context.resources.getDimension(it) } ?: defaultElevation

        return TransitionHelper.ContainerTransformOptions(
            startColor, endColor, startElevation, endElevation
        )
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
}
