package com.louis.app.cavity.ui.home.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import com.louis.app.cavity.R
import kotlin.math.round

class HexagonalView2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val HEXAGONAL_SQUARE_RATIO = 0.866
    }

    private val path = Path().apply {
        fillType = Path.FillType.EVEN_ODD
    }

    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, android.R.color.holo_green_light)
            style = Paint.Style.FILL_AND_STROKE
        }
    }

    private var isFlat = false

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.HexagonalView,
            defStyleAttr,
            0
        )
            .use {
                isFlat = it.getBoolean(R.styleable.HexagonalView_flat, false)
            }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSpec = widthMeasureSpec
        var heightSpec = heightMeasureSpec

        if (isFlat) {
            // Force our width based on height to get perfect hexagonal shape
            widthSpec = MeasureSpec.makeMeasureSpec(
                round(MeasureSpec.getSize(heightMeasureSpec) / HEXAGONAL_SQUARE_RATIO).toInt(),
                MeasureSpec.EXACTLY
            )
        } else {
            // Force our height based on width to get perfect hexagonal shape
            heightSpec = MeasureSpec.makeMeasureSpec(
                round(MeasureSpec.getSize(widthMeasureSpec) / HEXAGONAL_SQUARE_RATIO).toInt(),
                MeasureSpec.EXACTLY
            )
        }

        super.onMeasure(widthSpec, heightSpec)
    }

    override fun onDraw(canvas: Canvas) {
        val largerSide = if (isFlat) width.toFloat() else height.toFloat()
        val smallerSide = if (isFlat) height.toFloat() else width.toFloat()

        path.run {
            moveTo(0f, largerSide / 2)
            lineTo(0f, largerSide * 0.25f)
            lineTo(smallerSide / 2, 0f)
            lineTo(smallerSide, largerSide * 0.25f)
            lineTo(smallerSide, largerSide * 0.75f)
            lineTo(smallerSide / 2, largerSide)
            lineTo(0f, largerSide * 0.75f)
            lineTo(0f, largerSide / 2)
            close()
        }

        canvas.drawPath(path, paint)
    }
}
