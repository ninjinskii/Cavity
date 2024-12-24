package com.louis.app.cavity.ui.home.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable

class UnderlayDrawerArrowDrawable(
    context: Context,
    @Px private val cornerRadius: Float,
    @Px private val padding: Float,
    @ColorInt private val color: Int
) :
    DrawerArrowDrawable(context) {

    private val backgroundPath = Path()
    private val paddedRect = RectF()

    private val backgroundPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this@apply.color = this@UnderlayDrawerArrowDrawable.color
        }
    }

    override fun draw(canvas: Canvas) {
        with(bounds) {
            paddedRect.set(left - padding, top - padding, right + padding, bottom + padding)
        }

        backgroundPath.reset()
        backgroundPath.addRoundRect(
            paddedRect,
            cornerRadius,
            cornerRadius,
            Path.Direction.CW
        )

        canvas.drawPath(backgroundPath, backgroundPaint)
        super.draw(canvas)
    }
}
