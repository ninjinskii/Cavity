package com.louis.app.cavity.ui.widget

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.withTranslation
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.louis.app.cavity.R
import com.louis.app.cavity.db.dao.NewStat
import com.louis.app.cavity.util.ColorUtil
import com.louis.app.cavity.util.dpToPx
import kotlin.math.cos
import kotlin.math.roundToInt

class SliceBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {

    companion object {
        private const val BAR_BOTTOM_SPACING = 16f
        private const val BAR_WIDTH = 4f
        private const val TEXT_ANGLE = 50f
    }

    private val touchListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            showTooltipOnClick(e.x)
            return super.onSingleTapUp(e)
        }
    }

    private val detector: GestureDetector = GestureDetector(context, touchListener)

    private val slices = mutableListOf<NewStat>()
    private val backgroundColor = context.getColor(R.color.cavity_grey)
    private val colors = ColorUtil(context).randomSet()

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

    private var interpolation = 1f
        set(value) {
            val constrained = value.coerceIn(0F, 1F)
            if (constrained != field) {
                field = constrained
                invalidate()
            }
        }

    private var progressUnitPixelSize = 1f
    private var baseline = 0f
    private var textMaxLength = 0f
    private var startX = 0f
    private var endX = 0f
    private var barY = 0f

    fun setSlices(slices: List<NewStat>, anim: Boolean) {
        val empty: Boolean

        this.slices.apply {
            empty = isEmpty()
            clear()
            addAll(slices)
            sortByDescending { it.percentage }
        }

        if (empty && anim) {
            triggerAnimation()
        } else {
            invalidate()
        }
    }

    fun triggerAnimation() {
        ObjectAnimator.ofFloat(this, "interpolation", 0f, 1f).apply {
            duration = 800
            interpolator = FastOutSlowInInterpolator()
            start()
        }
    }

    private fun showTooltipOnClick(touchX: Float) {
        var x = 0f
        val touchPercentage = (touchX / measuredWidth) * 100
        val touchedSlice = slices.find { stat ->
            touchPercentage in x..x + stat.percentage.also { x += it }
        }

        touchedSlice?.let {
            TooltipCompat.setTooltipText(
                this@SliceBarView,
                "${it.label}: ${it.percentage.roundToInt()}%"
            )

            this@SliceBarView.performLongClick(touchX, barY)
        }
    }

    // Dont know what to do. We need coordinates, but performClick() does not take any args
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return detector.onTouchEvent(event)
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

            slices.forEachIndexed { i, stat ->
                strokePaint.color = colors[i % colors.size]

                val progress = stat.percentage * progressUnitPixelSize * interpolation
                drawLine(currentPixel, barY, currentPixel + progress, barY, strokePaint)

                textPaint.textSize = if (stat.percentage <= 5) 20f else 30f

                val text = TextUtils.ellipsize(
                    stat.label,
                    textPaint,
                    textMaxLength,
                    TextUtils.TruncateAt.END
                )

                canvas.withTranslation(currentPixel + (progress / 2f), baseline) {
                    rotate(TEXT_ANGLE)
                    drawText(text.toString(), 0f, 0f, textPaint)
                }

                currentPixel += progress
            }
        }
    }
}
