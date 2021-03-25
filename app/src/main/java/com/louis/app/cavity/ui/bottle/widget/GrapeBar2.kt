package com.louis.app.cavity.ui.bottle.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.louis.app.cavity.R
import com.louis.app.cavity.model.relation.grape.QuantifiedGrapeAndGrape

class GrapeBar2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    View(context, attrs, defStyleAttr) {

    private val grapes = mutableListOf<QuantifiedGrapeAndGrape>()
    private val backgroundColor = context.getColor(R.color.cavity_grey)
    private val colors = listOf(
        R.color.cavity_red,
        R.color.cavity_brown,
        R.color.cavity_light_green,
        R.color.cavity_indigo,
        R.color.cavity_purple,
        R.color.cavity_yellow
    )
        .map { context.getColor(it) }
        .shuffled()

    private val strokePaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            // TO DO: use dp
            strokeWidth = 15f
        }
    }

    private var pixelProgressRatio = 1
    private var startX = 0f
    private var endX = 0f

    fun setGrapes(grapes: List<QuantifiedGrapeAndGrape>) {
        this.grapes.clear()
        this.grapes.addAll(grapes)
        // TODO: Maybe requestLayout for text
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val paddingX = paddingStart + paddingEnd
        startX = paddingStart.toFloat()
        endX = (w - paddingEnd).toFloat()
        pixelProgressRatio = (w - paddingX) / 100
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = 0

        when (heightMode) {
            MeasureSpec.EXACTLY -> height = MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.AT_MOST -> height = MeasureSpec.getSize(heightMeasureSpec)
            // TODO: compute height with the longest string
            MeasureSpec.UNSPECIFIED -> height = 50
        }

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            strokePaint.color = backgroundColor
            drawLine(startX, 0f, endX, 0f, strokePaint)

            var currentPixel = startX

            grapes.forEachIndexed { i, grape ->
                strokePaint.color = colors[i]

                val progress = grape.qGrape.percentage * pixelProgressRatio

                drawLine(currentPixel, 0f, currentPixel + (progress), 0f, strokePaint)
                currentPixel += progress
            }
        }
    }

    // Save state for colors ?
}
