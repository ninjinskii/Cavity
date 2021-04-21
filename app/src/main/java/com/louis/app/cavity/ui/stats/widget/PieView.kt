package com.louis.app.cavity.ui.stats.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.louis.app.cavity.ui.stats.StatsUiModel
import com.louis.app.cavity.util.dpToPx
import kotlin.math.min

class PieView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {

    private val strokeWidth = context.dpToPx(6f)
    private val sliceSpace = context.dpToPx(1f)

    private val piePaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = this@PieView.strokeWidth
            color = Color.BLACK
        }
    }

    private var pieData: List<StatsUiModel.Pie.PieSlice> = mutableListOf()

    private var rect = RectF()
    private var pieRadius = 0f
    private var centerX = 0.0f
    private var centerY = 0.0f

    fun setPieData(data: List<StatsUiModel.Pie.PieSlice>) {
        pieData = data
        invalidate()
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

        pieData.forEach {
            // TODO: get a color on a RandomColorGenerator when fetching data on ViewModel
            piePaint.color = it.color ?: Color.BLUE

            val startAngle = previousAngle + sliceSpace
            val sweepAngle = it.angle - sliceSpace

            canvas.drawArc(rect, startAngle, sweepAngle, false, piePaint)

            previousAngle += it.angle
        }
    }
}
