package com.louis.app.cavity.ui.home.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.graphics.toRectF
import androidx.core.graphics.toRegion
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapeAppearancePathProvider
import com.louis.app.cavity.R
import com.louis.app.cavity.util.dpToPx
import kotlin.math.round

/**
 * A CardView that forces its shape to be a perfect hexagone. Thus, you can only control width or
 * height (but never both at the same time) of the view depending on the flat attribute.
 * Doesn't support padding for now, since it could break the perfect hexagonal shape.
 */
class HexagonalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.materialCardViewStyle,
    defStyleRes: Int = R.style.Widget_MaterialComponents_CardView
) :
    MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private const val HEXAGONAL_SQUARE_RATIO = 0.866
    }

    private val clickableArea = Region()
    private val clipPath = Path()
    private val markerPath = Path()
    private val clipPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, android.R.color.white)
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        }
    }

    private val markerPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = markerColor.defaultColor
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = context.dpToPx(16f)
        }
    }

    private var isFlat = false
    private var markerColor: ColorStateList = ColorStateList.valueOf(Color.TRANSPARENT)

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.HexagonalView,
            defStyleAttr,
            defStyleRes
        )
            .use {
                isFlat = it.getBoolean(R.styleable.HexagonalView_flat, false)
                markerColor = it.getColorStateList(R.styleable.HexagonalView_markerColor)
                    ?: ColorStateList.valueOf(Color.TRANSPARENT)
            }

        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        applyShape()
    }

    fun setMarkerColor(color: Int) {
        markerPaint.color = color
        invalidate()
    }

    private fun applyShape() {
        val topRightBottomLeftCorners = HexagonalCornerTreatment(!isFlat)
        val topLeftBottomRightCorners = HexagonalCornerTreatment(isFlat)

        shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes { if (isFlat) it.height() / 2 else it.width() / 2 }
            .setTopLeftCorner(topLeftBottomRightCorners)
            .setTopRightCorner(topRightBottomLeftCorners)
            .setBottomRightCorner(topLeftBottomRightCorners)
            .setBottomLeftCorner(topRightBottomLeftCorners)
            .build()
    }

    // Oh yeah? And what should I do then ?
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()

        if (!clickableArea.contains(x, y) && event.action == MotionEvent.ACTION_DOWN) {
            return false
        }

        return super.onTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        markerPath.run {
            if (isFlat) {
                moveTo(w * 0.25f, h.toFloat())
                lineTo(0f, h /2f)
                lineTo(0f, h.toFloat())
                close()
            } else {
                moveTo(w / 2f, h.toFloat())
                lineTo(0f, h * 0.75f)
                lineTo(0f, h.toFloat())
                close()
            }
        }

        val r = Rect(0, 0, w, h).toRectF()
        ShapeAppearancePathProvider().calculatePath(shapeAppearanceModel, 1f, r, clipPath)
        clickableArea.setPath(clipPath, r.toRegion())

        // Reverse the given path to get correct clipping out of it
        clipPath.fillType = Path.FillType.INVERSE_WINDING
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSpec = widthMeasureSpec
        var heightSpec = heightMeasureSpec

        if (isFlat) {
            // Force our width based on height to get perfect hexagonal shape
            widthSpec = makeMeasureSpec(
                round(getSize(heightMeasureSpec) / HEXAGONAL_SQUARE_RATIO).toInt(),
                EXACTLY
            )
        } else {
            // Force our height based on width to get perfect hexagonal shape
            heightSpec = makeMeasureSpec(
                round(getSize(widthMeasureSpec) / HEXAGONAL_SQUARE_RATIO).toInt(),
                EXACTLY
            )
        }

        super.onMeasure(widthSpec, heightSpec)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

        super.dispatchDraw(canvas)

        canvas.run {
            drawPath(markerPath, markerPaint)
            drawPath(clipPath, clipPaint)
            restoreToCount(saveCount)
        }
    }

    // Ignoring super call here would fix a bug for wine's RecyclerView when Glide attempts to load
    // images. Doesn't seems to be necessary anymore.
    override fun requestLayout() {
        super.requestLayout()
    }
}
