package com.louis.app.cavity.ui.home.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.updatePadding
import com.google.android.material.appbar.MaterialToolbar
import com.louis.app.cavity.R
import com.louis.app.cavity.util.dpToPx

class TransluscentToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) :
    MaterialToolbar(context, attrs) {

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

    fun setOnTitleClickListener(listener: (View) -> Unit) {
        this.children.firstOrNull { it is TextView }?.apply {
            isClickable = true
            isFocusable = true
            this as TextView
            setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_flat_arrow_down, 0)
            compoundDrawablePadding = context.resources.getDimension(R.dimen.small_margin).toInt()
            setOnClickListener(listener)
        }
    }

    override fun onTouchEvent(ev: MotionEvent?) = false.also { performClick() }

    override fun performClick() = false.also { super.performClick() }
}
