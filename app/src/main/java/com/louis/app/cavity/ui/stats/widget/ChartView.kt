package com.louis.app.cavity.ui.stats.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.louis.app.cavity.util.dpToPx
import kotlin.math.max

class PieView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {

    companion object {
        private const val STROKE_WIDTH = 6f
    }

    private var rect = RectF()
    private var pieRadius = 0f
    private var centerX = 0.0f
    private var centerY = 0.0f

    private val piePaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = context.dpToPx(STROKE_WIDTH)
            color = Color.BLUE
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val ww = w + paddingLeft + paddingRight
        val hh = h + paddingTop + paddingBottom

        pieRadius = max(ww, hh) / 2f

        rect.set(
            0f + STROKE_WIDTH + paddingLeft,
            0f + STROKE_WIDTH + paddingTop,
            width - STROKE_WIDTH - paddingRight,
            height - STROKE_WIDTH - paddingRight
        )

        centerX = rect.centerX()
        centerY = rect.centerY()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}
