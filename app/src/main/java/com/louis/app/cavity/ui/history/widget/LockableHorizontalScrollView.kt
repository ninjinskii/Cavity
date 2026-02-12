package com.louis.app.cavity.ui.history.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView

class LockableHorizontalScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    HorizontalScrollView(context, attrs, defStyleAttr) {

    var isScrollEnabled = true
        set(value) {
            if (!value) scrollTo(0, 0)
            field = value
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (isScrollEnabled) {
            super.onTouchEvent(event)
        } else {
            false
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        return if (isScrollEnabled) {
            super.onInterceptTouchEvent(event)
        } else {
            false
        }
    }
}
