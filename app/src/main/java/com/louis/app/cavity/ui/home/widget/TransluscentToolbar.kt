package com.louis.app.cavity.ui.home.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.updatePadding
import com.google.android.material.appbar.MaterialToolbar
import com.louis.app.cavity.R

class TransluscentToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.toolbarStyle,
) :
    MaterialToolbar(context, attrs, defStyleAttr) {

    init {
        this.post {
            val cornerRadius = resources.getDimension(R.dimen.small_margin)
            val color = ContextCompat.getColor(context, R.color.title_background)

            navigationIcon?.let {
                val currentProgress = (it as? DrawerArrowDrawable)?.progress ?: 0f
                navigationIcon =
                    UnderlayDrawerArrowDrawable(context, cornerRadius, cornerRadius, color).apply {
                        progress = currentProgress
                    }
            }

            this.children.firstOrNull { it is TextView }?.let { titleTextView ->
                titleTextView.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.shape_background_title,
                    context.theme
                )

                val paddingVertical = this@TransluscentToolbar.height / 2 - titleTextView.height
                titleTextView.updatePadding(
                    left = cornerRadius.toInt(),
                    top = paddingVertical,
                    right = cornerRadius.toInt(),
                    bottom = paddingVertical
                )
            }
        }
    }

    override fun onTouchEvent(ev: MotionEvent?) = false.also { performClick() }

    override fun performClick() = false.also { super.performClick() }
}


/*class UnderlayMergeDrawable(
    private val drawable: Drawable,
    private val cornerRadius: Float,
    @ColorInt private val color: Int
) :
    Drawable() {

    private val backgroundPath = Path()

    private val backgroundPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this@apply.color = this@UnderlayMergeDrawable.color
        }
    }

    override fun setAlpha(p0: Int) {
    }

    override fun setColorFilter(p0: ColorFilter?) {
    }

    @Deprecated(
        "Superclass method is deprecated",
        ReplaceWith("PixelFormat.UNKNOWN", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }

    override fun draw(canvas: Canvas) {
        val centerX = drawable.bounds.exactCenterX()
        val centerY = drawable.bounds.exactCenterY()
        val length = min(drawable.bounds.width(), drawable.bounds.height())
        val left = centerX - (length / 2)
        val top = centerY - (length / 2)
        val right = centerX + (length / 2)
        val bottom = centerY + (length / 2)
        val ratioRect = RectF(left, top, right, bottom)

        backgroundPath.reset()
        backgroundPath.addRoundRect(ratioRect, cornerRadius, cornerRadius, Path.Direction.CW)

        canvas.drawPath(backgroundPath, backgroundPaint)
        this.copyBounds(drawable.bounds)
        drawable.draw(canvas)
    }
}*/
