package com.louis.app.cavity.ui.history.adapter

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.AnimatedStateListDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.res.ResourcesCompat
import com.louis.app.cavity.R
import org.xmlpull.v1.XmlPullParser

class HistorySwipeActionDrawable : Drawable() {
    private lateinit var icon: AnimatedStateListDrawable
    private val bg = ColorDrawable(Color.parseColor("#000000"))
    private val iconRect = Rect()
    @Px
    private var iconSize = 0
    @Px
    private var iconMargin = 0
    @Px
    private var iconTop = 0
    @ColorInt
    private var colorPrimary = 0

    override fun onBoundsChange(bounds: Rect?) {
        if (bounds == null) return

        iconTop = bounds.centerY() - (iconSize / 2)
        callback?.invalidateDrawable(this)
    }

    override fun isStateful() = true

    override fun onStateChange(state: IntArray?): Boolean {
        val shouldRedraw: Boolean

        if (state?.contains(android.R.attr.state_activated) == true) {
            shouldRedraw = !icon.state.contains(android.R.attr.state_activated)
            icon.state = intArrayOf(android.R.attr.state_activated)
        } else {
            shouldRedraw = icon.state.contains(android.R.attr.state_activated)
            icon.state = intArrayOf()
        }

        return shouldRedraw
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
        iconSize = r.getDimensionPixelSize(R.dimen.xsmall_icon)
        iconMargin = r.getDimensionPixelSize(R.dimen.medium_margin)
        colorPrimary = ResourcesCompat.getColor(r, R.color.cavity_gold, theme)
        icon =
            ResourcesCompat.getDrawable(r, R.drawable.asl_star, theme) as AnimatedStateListDrawable
    }

    override fun draw(canvas: Canvas) {
        val w = bounds.width()

        with(iconRect) {
            left = w - iconSize - iconMargin
            top = iconTop
            right = w - iconMargin
            bottom = iconTop + iconSize
        }

        bg.bounds = Rect(0, 0, w, bounds.height())
        bg.draw(canvas)

        icon.bounds = iconRect
        icon.setTint(colorPrimary)
        icon.draw(canvas)
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
