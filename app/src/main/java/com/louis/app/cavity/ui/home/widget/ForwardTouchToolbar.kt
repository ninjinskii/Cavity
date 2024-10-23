package com.louis.app.cavity.ui.home.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.appbar.MaterialToolbar

class ForwardTouchToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.toolbarStyle,
) :
    MaterialToolbar(context, attrs, defStyleAttr) {

    override fun onTouchEvent(ev: MotionEvent?) = false.also { performClick() }

    override fun performClick() = false.also { super.performClick() }
}
