package com.louis.app.cavity.ui.history.adapter

import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.AnimatedStateListDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.Px
import androidx.core.content.res.ResourcesCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.louis.app.cavity.R
import org.xmlpull.v1.XmlPullParser
import kotlin.math.abs

class HistorySwipeActionDrawable : Drawable() {
    private lateinit var icon: AnimatedStateListDrawable
    private lateinit var gradient: Drawable
    private val dur = 800f
    private val iconRect = Rect()
    private val gradientRect = Rect()
    private var animator: ValueAnimator? = null
    @Px
    private var iconSize: Int = 0
    @Px
    private var iconMargin: Int = 0
    @Px
    private var iconTop: Int = 0

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

        iconTop = bounds.centerY() - (iconSize / 2)
        callback?.invalidateDrawable(this)
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

    override fun inflate(
        r: Resources,
        parser: XmlPullParser,
        attrs: AttributeSet,
        theme: Resources.Theme?
    ) {
        initResources(r, theme)
        super.inflate(r, parser, attrs, theme)
    }

    fun initResources(r: Resources, theme: Resources.Theme?) {
        gradient = ResourcesCompat.getDrawable(r, R.drawable.gradient_star, theme)!!
        iconSize = r.getDimensionPixelSize(R.dimen.medium_icon)
        iconMargin = r.getDimensionPixelSize(R.dimen.large_margin)
        icon =
            ResourcesCompat.getDrawable(r, R.drawable.asl_star, theme) as AnimatedStateListDrawable
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

        with(gradientRect) {
            left = w - 400
            top = 0
            right = w
            bottom = h
        }

        icon.bounds = iconRect
        icon.draw(canvas)

        gradient.bounds = gradientRect
        gradient.draw(canvas)
    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {
        icon.alpha = alpha
        gradient.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        icon.colorFilter = colorFilter
    }
}
