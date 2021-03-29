package com.louis.app.cavity.ui.bottle.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.louis.app.cavity.R
import com.louis.app.cavity.model.relation.grape.QuantifiedGrapeAndGrape
import com.louis.app.cavity.util.dpToPx
import kotlin.math.cos

class GrapeBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) :
    View(context, attrs, defStyleAttr) {

    companion object {
        private const val BAR_BOTTOM_SPACING = 16f
        private const val BAR_WIDTH = 4f
        private const val TEXT_ANGLE = 50f
    }

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
            strokeWidth = context.dpToPx(BAR_WIDTH)
        }
    }

    private val textPaint by lazy {
        TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.material_on_surface_emphasis_medium)
        }
    }

    var interpolation = 1f
        set(value) {
            field = value
            invalidate()
        }

    private var progressUnitPixelSize = 1f
    private var baseline = 0f
    private var textMaxLength = 0f
    private var startX = 0f
    private var endX = 0f
    private var barY = 0f

    fun setGrapes(grapes: List<QuantifiedGrapeAndGrape>, anim: Boolean) {
        val empty: Boolean

        this.grapes.apply {
            empty = isEmpty()
            clear()
            addAll(grapes)
            sortBy { it.qGrape.percentage }
        }

        if (empty && anim) {
            ObjectAnimator.ofFloat(this, "interpolation", 0f, 1f).apply {
                duration = 800
                interpolator = FastOutSlowInInterpolator()
                start()
            }
        } else {
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val paddingX = paddingStart + paddingEnd
        val spacing = context.dpToPx(BAR_BOTTOM_SPACING)
        progressUnitPixelSize = (w - paddingX) / 100f
        startX = paddingStart.toFloat()
        endX = (w - paddingEnd).toFloat()
        barY = (strokePaint.strokeWidth / 2) + paddingTop
        baseline = barY + strokePaint.strokeWidth + spacing
        textMaxLength = (h - baseline) / cos(TEXT_ANGLE)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = 0

        when (heightMode) {
            MeasureSpec.EXACTLY -> height = MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.AT_MOST -> height = MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.UNSPECIFIED -> height = 50
        }

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            strokePaint.color = backgroundColor
            drawLine(startX, barY, endX, barY, strokePaint)

            var currentPixel = startX

            grapes.forEachIndexed { i, grape ->
                strokePaint.color = colors[i % colors.size]

                val progress = grape.qGrape.percentage * progressUnitPixelSize * interpolation
                drawLine(currentPixel, barY, currentPixel + progress, barY, strokePaint)

                textPaint.textSize = if (grape.qGrape.percentage <= 5) 20f else 30f

                val saveCount = save()
                val text = TextUtils.ellipsize(
                    grape.grapeName,
                    textPaint,
                    textMaxLength,
                    TextUtils.TruncateAt.END
                )

                translate(currentPixel + (progress / 2f), baseline)
                rotate(TEXT_ANGLE)
                drawText(text.toString(), 0f, 0f, textPaint)
                restoreToCount(saveCount)

                currentPixel += progress
            }
        }
    }
}
