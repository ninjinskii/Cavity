package com.louis.app.cavity.ui.bottle.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.louis.app.cavity.model.Grape

class GrapeBar2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    View(context, attrs, defStyleAttr) {

    val grapes = mutableListOf<Grape>()
    val strokePaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
    }

    // Maybe calculate ratio pixel / progress in on size changes and stor it into var

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()

        canvas.apply {
            strokePaint.color = Color.GRAY
            drawLine(0f, 0f, w, 0f, strokePaint)

            grapes.forEach {

            }
        }

    }
}
