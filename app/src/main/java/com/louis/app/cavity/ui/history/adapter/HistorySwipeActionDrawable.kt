package com.louis.app.cavity.ui.history.adapter

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.AnimatedStateListDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.res.ResourcesCompat
import com.louis.app.cavity.R

class HistorySwipeActionDrawable(resources: Resources, theme: Resources.Theme?) : Drawable() {
    private val icon = ResourcesCompat.getDrawable(resources, R.drawable.asl_star, theme)
        as AnimatedStateListDrawable

    @Px
    private val iconSize = resources.getDimensionPixelSize(R.dimen.xsmall_icon)

    @Px
    private val iconMargin = resources.getDimensionPixelSize(R.dimen.medium_margin)

    @Px
    private var iconTop = 0

    @ColorInt
    private val colorPrimary = ResourcesCompat.getColor(resources, R.color.cavity_gold, theme)

    private val bg = ColorDrawable(Color.BLACK)
    private val iconRect = Rect()
    private val bgRect = Rect()

    override fun onBoundsChange(bounds: Rect?) {
        if (bounds == null) return

        iconTop = bounds.centerY() - (iconSize / 2)
        callback?.invalidateDrawable(this)
    }

    override fun isStateful() = true

    override fun jumpToCurrentState() {
        icon.jumpToCurrentState()
    }

    override fun onStateChange(state: IntArray): Boolean {
        icon.state = state
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
    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {
        icon.alpha = alpha
        bg.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        icon.colorFilter = colorFilter
    }
}
