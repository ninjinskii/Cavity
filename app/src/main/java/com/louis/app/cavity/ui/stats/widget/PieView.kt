package com.louis.app.cavity.ui.stats.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.louis.app.cavity.R
import com.louis.app.cavity.db.dao.Stat
import com.louis.app.cavity.util.spToPx
import kotlin.math.PI
import kotlin.math.min

class PieView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {

    companion object {
        private const val TEXT_SIZE = 12f
    }

    private val sliceSpace = resources.getDimension(R.dimen.divider_height) / 2
    private val strokeWidth = resources.getDimension(R.dimen.pie_stroke_width)
    private val textSpace = resources.getDimension(R.dimen.pie_text_space)
    private val upsideDownTextSpace = resources.getDimension(R.dimen.pie_upside_down_text_space)

    private val backgroundColor = Color.BLACK
    private val transparent = Color.TRANSPARENT
    private val textColor = ContextCompat.getColor(
        context,
        R.color.material_on_surface_emphasis_medium
    )

    private val textPath = Path()

    private val piePaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = this@PieView.strokeWidth
        }
    }

    private val textPaint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = context.spToPx(TEXT_SIZE)
        }
    }

    private var pieSlices = emptyList<Stat>()
    private var rect = RectF()
    private var pieRadius = 0f
    private var centerX = 0.0f
    private var centerY = 0.0f

    private var interpolation = 1f
        set(value) {
            val constrained = value.coerceIn(0F, 1F)
            if (constrained != field) {
                field = constrained
                invalidate()
            }
        }

    fun setPieSlices(slices: List<Stat>, anim: Boolean) {
        if (anim) {
            if (pieSlices.isNotEmpty()) {
                replaceData(slices) // Will take care of updating pieData
            } else {
                pieSlices = slices
                triggerAnimation()
            }
        } else {
            pieSlices = slices
            invalidate()
        }
    }

    private fun triggerAnimation() {
        this.alpha = 1f

        ObjectAnimator.ofFloat(this, "interpolation", 0f, 1f).apply {
            duration = 800
            interpolator = FastOutSlowInInterpolator()
            start()
        }
    }

    private fun replaceData(slices: List<Stat>) {
        ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addListener(onEnd = { pieSlices = slices; triggerAnimation() })
            start()
        }
    }

    private fun getArcLength(sweepAngle: Float) =
        ((2f * PI.toFloat() * pieRadius) / 360f) * sweepAngle

    private fun getAngle(textLength: Float) = (360f * textLength) / (2 * PI.toFloat() * pieRadius)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val ww = w + paddingLeft + paddingRight
        val hh = h + paddingTop + paddingBottom

        pieRadius = min(ww, hh) / 2f

        rect.set(
            0f + strokeWidth + paddingLeft,
            0f + strokeWidth + paddingTop,
            pieRadius * 2 - strokeWidth - paddingRight,
            pieRadius * 2 - strokeWidth - paddingBottom
        )

        centerX = rect.centerX()
        centerY = rect.centerY()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0)

        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        piePaint.color = backgroundColor

        canvas.drawCircle(centerX, centerY, pieRadius - strokeWidth, piePaint)

        var previousAngle = -90f

        pieSlices.forEach {
            piePaint.color = context.getColor(it.color)
            textPaint.color = ColorUtils.blendARGB(transparent, textColor, interpolation)

            val startAngle = (previousAngle + sliceSpace) * interpolation
            val pureAngle = (it.percentage / 100) * 360
            val sweepAngle = (pureAngle - sliceSpace) * interpolation

            textPath.reset()

            // Might reverse arc to avoid drawing upside-down text
            val verticalOffset = if (startAngle in 0f..180f) {
                val sweepSpaceForText = min(sweepAngle, getAngle(textPaint.measureText(it.label)))
                textPath.addArc(rect, startAngle + sweepSpaceForText, -sweepSpaceForText)
                textSpace
            } else {
                textPath.addArc(rect, startAngle, sweepAngle)
                upsideDownTextSpace
            }

            val text = TextUtils.ellipsize(
                it.label,
                textPaint,
                getArcLength(sweepAngle),
                TextUtils.TruncateAt.END
            )

            canvas.drawTextOnPath(text.toString(), textPath, 0f, verticalOffset, textPaint)
            canvas.drawArc(rect, startAngle, sweepAngle, false, piePaint)

            previousAngle += pureAngle
        }
    }
}
