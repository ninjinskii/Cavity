package com.louis.app.cavity.ui.history.adapter

import android.animation.ObjectAnimator
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.AnimatedStateListDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.animation.BounceInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.res.ResourcesCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.louis.app.cavity.R

class HistorySwipeActionDrawable(resources: Resources, theme: Resources.Theme?) : Drawable() {
    private val icon = ResourcesCompat.getDrawable(resources, R.drawable.asl_star, theme)
        as AnimatedStateListDrawable

    @Px
    private val iconSize = resources.getDimensionPixelSize(R.dimen.xsmall_icon)

    @Px
    private val iconMargin = resources.getDimensionPixelSize(R.dimen.medium_margin)

    @Px
    private val circleRadius = resources.getDimensionPixelSize(R.dimen.starred_corner_size)

    @Px
    private var iconTop = 0

    @ColorInt
    private val colorPrimary = ResourcesCompat.getColor(resources, R.color.cavity_gold, theme)

    @ColorInt
    private val colorUnderSurface =
        ResourcesCompat.getColor(resources, R.color.under_surface, theme)

    private val bg = ColorDrawable(colorUnderSurface)
    private val iconRect = Rect()
    private val bgRect = Rect()
    private val gradient = intArrayOf(
        colorPrimary,
        ResourcesCompat.getColor(resources, R.color.cavity_gold_gradient_1, theme),
        colorPrimary,
        ResourcesCompat.getColor(resources, R.color.cavity_gold_gradient_2, theme),
        colorPrimary
    )

    private val isActivated
        get() = android.R.attr.state_activated in state

    private val activateAnimator = ObjectAnimator.ofFloat(this, "interpolation", 0f, 1f).apply {
        duration = SHOW_CIRCLE_DURATION
        interpolator = BounceInterpolator()
    }

    private val deactivateAnimator = ObjectAnimator.ofFloat(this, "interpolation", 1f, 0f).apply {
        duration = HIDE_CIRCLE_DURATION
        interpolator = FastOutSlowInInterpolator()
    }

    private val animatorWithFinalInterpolation =
        mapOf(activateAnimator to 1f, deactivateAnimator to 0f)

    private val circlePaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                0f,
                0f,
                circleRadius.toFloat(),
                gradient,
                floatArrayOf(0f, 0.3f, 0.7f, 0.8f, 0.9f),
                Shader.TileMode.MIRROR
            )
        }
    }

    private var circleAnimator = activateAnimator
    private var interpolation = 1f
        set(value) {
            val constrained = value.coerceIn(0F, 1F)
            if (constrained != field) {
                field = constrained
                invalidateSelf()
            }
        }

    override fun onBoundsChange(bounds: Rect?) {
        if (bounds == null) return

        iconTop = bounds.centerY() - (iconSize / 2)
        callback?.invalidateDrawable(this)
    }

    override fun isStateful() = true

    override fun jumpToCurrentState() {
        icon.jumpToCurrentState()

        updateCircleAnimator()
        circleAnimator.end()
    }

    override fun onStateChange(state: IntArray): Boolean {
        icon.state = state

        updateCircleAnimator()
        startCircleAnim()

        return false
    }

    override fun draw(canvas: Canvas) {
        val w = bounds.width()
        val h = bounds.height()

        with(iconRect) {
            left = w - iconSize - iconMargin
            top = iconTop
            right = w - iconMargin
            bottom = iconTop + iconSize
        }

        with(bgRect) {
            left = 0
            top = 0
            right = w
            bottom = h
        }

        bg.run {
            bounds = bgRect
            draw(canvas)
        }

        icon.run {
            bounds = iconRect
            setTint(colorPrimary)
            draw(canvas)
        }

        val interpolatedRadius = circleRadius * interpolation
        canvas.drawCircle(w.toFloat(), 0f, interpolatedRadius, circlePaint)
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("Used by Android. No need to replace", "")
    )
    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {
        icon.alpha = alpha
        bg.alpha = alpha
        circlePaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        icon.colorFilter = colorFilter
    }

    private fun updateCircleAnimator() {
        circleAnimator.end()
        circleAnimator = if (isActivated) activateAnimator else deactivateAnimator
    }

    private fun startCircleAnim() {
        val intendedInterpolation = animatorWithFinalInterpolation[circleAnimator]
        val shouldStart = interpolation != intendedInterpolation

        if (shouldStart) {
            circleAnimator.start()
        }
    }

    companion object {
        private const val HIDE_CIRCLE_DURATION = 800L
        private const val SHOW_CIRCLE_DURATION = 1400L
    }
}
