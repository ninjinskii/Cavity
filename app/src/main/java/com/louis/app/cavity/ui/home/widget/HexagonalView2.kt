package com.louis.app.cavity.ui.home.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import com.google.android.material.shape.ShapePath
import com.louis.app.cavity.R
import kotlin.math.round

class HexagonalView2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val HEXAGONAL_SQUARE_RATIO = 0.866
    }

    private val path = Path()

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

//        outlineProvider = object : ViewOutlineProvider() {
//            override fun getOutline(view: View, outline: Outline?) {
//                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
//                    outline?.setConvexPath(path)
//                } else {
//                    outline?.setPath(path)
//                }
//            }
//        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val largerSide = if (isFlat) w.toFloat() else h.toFloat()
        val smallerSide = if (isFlat) h.toFloat() else w.toFloat()

        path.run {
            moveTo(0f, largerSide / 2)
            lineTo(0f, largerSide * 0.25f)
            lineTo(smallerSide / 2, 0f)
            lineTo(smallerSide, largerSide * 0.25f)
            lineTo(smallerSide, largerSide * 0.75f)
            lineTo(smallerSide / 2, largerSide)
            lineTo(0f, largerSide * 0.75f)
            lineTo(0f, largerSide / 2)
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
        canvas.drawPath(path, paint)
    }
}
