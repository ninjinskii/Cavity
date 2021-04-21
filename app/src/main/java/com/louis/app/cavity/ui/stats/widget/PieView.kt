package com.louis.app.cavity.ui.stats.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.louis.app.cavity.ui.stats.StatsUiModel
import com.louis.app.cavity.util.ColorUtil
import com.louis.app.cavity.util.dpToPx
import kotlin.math.min

class PieView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {

    private val colorUtil = ColorUtil(context)
//    private val

    private val strokeWidth = context.dpToPx(6f)
    private val sliceSpace = context.dpToPx(1f)

    private val piePaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = this@PieView.strokeWidth
        }
    }

    private var pieData: List<StatsUiModel.Pie.PieSlice> = mutableListOf()
    private var colors = colorUtil.randomSet()

    private var rect = RectF()
    private var pieRadius = 0f
    private var centerX = 0.0f
    private var centerY = 0.0f

    var interpolation = 1f
        set(value) {
            val constrained = value.coerceIn(0F, 1F)
            if (constrained != field) {
                field = constrained
                invalidate()
            }
        }

    fun setPieData(data: List<StatsUiModel.Pie.PieSlice>, anim: Boolean) {
        pieData = data

        colors = if (data.any { it.color == null }) {
            colorUtil.randomSet()
        } else {
            data.map { ContextCompat.getColor(context, it.color!!) }
        }

        if (anim) {
            triggerAnimation()
        } else {
            invalidate()
        }
    }

    private fun triggerAnimation() {
        ObjectAnimator.ofFloat(this, "interpolation", 0f, 1f).apply {
            duration = 800
            interpolator = FastOutSlowInInterpolator()
            start()
        }
    }

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
        piePaint.color = Color.BLACK

        canvas.drawCircle(centerX, centerY, pieRadius - strokeWidth, piePaint)

        var previousAngle = -90f

        pieData.forEachIndexed { index, it ->
            piePaint.color = colors[index % colors.size]

            val startAngle = (previousAngle + sliceSpace) * interpolation
            val sweepAngle = (it.angle - sliceSpace) * interpolation

            canvas.drawArc(rect, startAngle, sweepAngle, false, piePaint)

            previousAngle += it.angle
        }
    }
}
