package com.louis.app.cavity.ui.home.widget

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.res.use
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.ShapeAppearanceModel
import com.louis.app.cavity.R
import kotlin.math.round

/**
 * A CardView that forces its shape to be a perfect hexagone. Thus, you can only control width or
 * height (but never both at the same time) of the view depending on the flat attribute.
 * Doesn't support padding for now, since it could break the perfect hexagonal shape.
 */
class HexagonalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    MaterialCardView(context, attrs, defStyleAttr) {

    companion object {
        private const val HEXAGONAL_SQUARE_RATIO = 0.866
    }

    private var isFlat = false

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.HexagonalView, defStyleAttr, 0)
            .use {
                isFlat = it.getBoolean(R.styleable.HexagonalView_flat, false)
            }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val toFlatCorner = HexagonalCornerTreatment(h.toFloat(), toFlatCorner = true)
        val toPointyCorner = HexagonalCornerTreatment(h.toFloat(), toFlatCorner = false)

        shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes { it.width() / 2 }
            .setTopLeftCorner(toPointyCorner)
            .setTopRightCorner(toFlatCorner)
            .setBottomRightCorner(toPointyCorner)
            .setBottomLeftCorner(toFlatCorner)
            .build()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var w = 0
        var h = 0

        if (isFlat) {

        } else {
            val minw = suggestedMinimumWidth
            w = resolveSizeAndState(minw, widthMeasureSpec, 1)

            // Force our height based on width to get perfect hexagonal shape
            h = round(w / HEXAGONAL_SQUARE_RATIO).toInt()
        }

        setMeasuredDimension(w, h)
    }
}
