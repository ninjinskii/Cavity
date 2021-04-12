package com.louis.app.cavity.ui.history

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.louis.app.cavity.R
import com.louis.app.cavity.util.L
import kotlin.math.abs

class HistorySwipeActionDrawable(context: Context) : Drawable() {
    private val dur = 800f
    private val colorPrimary = context.getColor(R.color.cavity_gold)
    private val colorMedalGold = context.getColor(R.color.medal_gold)
    private val colorBackground = Color.parseColor("#121212")
    private val iconMargin = context.resources.getDimensionPixelSize(R.dimen.large_margin)
    private val icon = ContextCompat.getDrawable(context, R.drawable.asl_star)!!
    private val gradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.BL_TR, intArrayOf(
            colorBackground,
            colorMedalGold,
            colorPrimary
        )
    ).apply { setGradientCenter(0.85f, 0f) }

    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var animator: ValueAnimator? = null

    private var progress = 0F
        set(value) {
            val constrained = value.coerceIn(0F, 1F)
            if (constrained != field) {
                field = constrained
                callback?.invalidateDrawable(this)
            }
        }

    override fun onBoundsChange(bounds: Rect?) {
        if (bounds == null) return
        L.v("On bounds change")
    }

    override fun isStateful() = true

    override fun onStateChange(state: IntArray?): Boolean {
        val initialProgress = progress
        val newProgress = if (state?.contains(android.R.attr.state_activated) == true) {
            icon.state = intArrayOf(android.R.attr.state_checked)
            1F
        } else {
            icon.state = intArrayOf()
            0F
        }

        animator?.cancel()
        animator = ValueAnimator.ofFloat(initialProgress, newProgress).apply {
            addUpdateListener {
                progress = animatedValue as Float
            }
            interpolator = FastOutSlowInInterpolator()
            duration = (abs(newProgress - initialProgress) * dur).toLong()
            start()
        }

        return newProgress == initialProgress
    }

    override fun draw(canvas: Canvas) {
        canvas.drawBitmap(
            gradientDrawable.toBitmap(100, intrinsicHeight),
            intrinsicWidth - 100f,
            0f,
            gradientPaint
        )

    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {
        gradientPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        gradientPaint.colorFilter = colorFilter
    }
}
