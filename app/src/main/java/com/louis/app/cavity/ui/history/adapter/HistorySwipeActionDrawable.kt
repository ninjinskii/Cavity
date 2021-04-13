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

class HistorySwipeActionDrawable : Drawable() {
    private lateinit var icon: AnimatedStateListDrawable
    private val bg = ColorDrawable(Color.parseColor("#000000"))
    private val iconRect = Rect()
    private val bgRect = Rect()

    @Px
    private var iconSize = 0

    @Px
    private var iconMargin = 0

    @Px
    private var iconTop = 0

    @ColorInt
    private var colorPrimary = 0

    fun initResources(r: Resources, theme: Resources.Theme?) {
        iconSize = r.getDimensionPixelSize(R.dimen.xsmall_icon)
        iconMargin = r.getDimensionPixelSize(R.dimen.medium_margin)
        colorPrimary = ResourcesCompat.getColor(r, R.color.cavity_gold, theme)
        icon = ResourcesCompat.getDrawable(r, R.drawable.asl_star, theme)
                as AnimatedStateListDrawable
    }

    override fun onBoundsChange(bounds: Rect?) {
        if (bounds == null) return

        iconTop = bounds.centerY() - (iconSize / 2)
        callback?.invalidateDrawable(this)
    }

    override fun isStateful() = true

    override fun jumpToCurrentState() {
        icon.jumpToCurrentState()
    }

    override fun onStateChange(state: IntArray?): Boolean {
        val isActivated = state?.contains(android.R.attr.state_activated) == true

        if (isActivated) {
            icon.state = icon.state + android.R.attr.state_activated
        } else {
            icon.state = icon.state.filter { it != android.R.attr.state_activated }.toIntArray()
        }

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
