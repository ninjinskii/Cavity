package com.louis.app.cavity.ui.widget

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.graphics.withTranslation
import androidx.core.widget.TextViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.louis.app.cavity.R
import com.louis.app.cavity.db.dao.Stat
import com.louis.app.cavity.util.dpToPx
import com.louis.app.cavity.util.spToPx
import com.louis.app.cavity.util.themeColor
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
        private const val TEXT_SIZE_NORMAL = 16f
        private const val TEXT_SIZE_SMALL = 12f
        private const val CURSOR_HEIGHT = 8f
    }

    private val backgroundColor = context.getColor(R.color.cavity_grey)
    private val cursorColor = context.themeColor(com.google.android.material.R.attr.colorOnSurface)
    private val normalText = context.spToPx(TEXT_SIZE_NORMAL)
    private val smallText = context.spToPx(TEXT_SIZE_SMALL)


    private val strokePaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = context.dpToPx(BAR_WIDTH)
        }
    }

    private val cursorPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = cursorColor
            strokeWidth = context.dpToPx(BAR_WIDTH / 2)
        }
    }

    private val textAppearanceApplier = AppCompatTextView(context).apply {
        TextViewCompat.setTextAppearance(this, R.style.TextAppearance_Cavity_Body1)
    }

    private val textPaint by lazy {
        textAppearanceApplier.paint.apply {
            color = ContextCompat.getColor(context, com.google.android.material.R.color.material_on_surface_emphasis_medium)
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

    private var waitAnimationTriggerBeforeDraw = false
    private var slices = emptyList<Stat>()
    private var previousTouchedSlice: Stat? = null
    private var progressUnitPixelSize = 1f
    private var baseline = 0f
    private var textMaxLength = 0f
    private var startX = 0f
    private var endX = 0f
    private var barY = 0f
    private var longPressX = -1f

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SliceBarView,
            defStyleAttr,
            0
        ).use {
            waitAnimationTriggerBeforeDraw = it.getBoolean(
                R.styleable.SliceBarView_waitAnimationTriggerBeforeDraw,
                false
            )
        }
    }

    // Android view attributes convention
    @Suppress("unused")
    fun setWaitAnimationTriggerBeforeDraw(wait: Boolean) {
        this.waitAnimationTriggerBeforeDraw = wait
    }

    fun setSlices(slices: List<Stat>, anim: Boolean) {
        val empty = this.slices.isEmpty()
        this.slices = slices

        if (empty && anim) {
            triggerAnimation()
        } else if (!waitAnimationTriggerBeforeDraw) {
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

    private fun showTooltipOnClick(touchX: Float, move: Boolean) {
        var x = 0f
        val touchPercentage = (touchX / measuredWidth) * 100
        val touchedSlice = slices.find { stat ->
            touchPercentage in x..x + stat.percentage.also { x += it }
        }

        touchedSlice?.apply {
            if (move && touchedSlice == previousTouchedSlice) {
                return
            }

            TooltipCompat.setTooltipText(
                this@SliceBarView,
                "${label}: ${percentage.roundToInt()}%"
            )

            if (move) {
                this@SliceBarView.performLongClick(touchX, barY)
            }
        }?.also { previousTouchedSlice = it }
    }

    // Dont know what to do. We need coordinates, but performClick() does not take any args
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> showTooltipOnClick(event.x, move = false)
            MotionEvent.ACTION_MOVE -> {
                showTooltipOnClick(event.x, move = true)
                longPressX = event.x
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                longPressX = -1f
                invalidate()
            }
        }

        return super.onTouchEvent(event)
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

        drawCursor(canvas)

        canvas.apply {
            strokePaint.color = backgroundColor
            drawLine(startX, barY, endX, barY, strokePaint)

            var currentPixel = startX

            slices.forEach { stat ->
                strokePaint.color = context.getColor(stat.color)

                val progress = stat.percentage * progressUnitPixelSize * interpolation
                drawLine(currentPixel, barY, currentPixel + progress, barY, strokePaint)

                textPaint.textSize = if (stat.percentage <= 5) smallText else normalText

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

    private fun drawCursor(canvas: Canvas) {
        // Only draw cursor if user is sliding his thumb along the SliceBar
        if (longPressX != -1f) {
            val cursorHeightDp = context.dpToPx(CURSOR_HEIGHT)
            val barhHeightDp = context.dpToPx(BAR_WIDTH)
            canvas.drawLine(
                longPressX,
                barY + barhHeightDp,
                longPressX,
                barY + barhHeightDp + cursorHeightDp,
                cursorPaint
            )
        }
    }
}
