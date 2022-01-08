package com.louis.app.cavity.ui.tasting

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

/**
 * A RecyclerView that don't catch touch event, to use as an inner nested RecyclerView and let
 * the parent item still catch events
 */
class ForwardTouchRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    RecyclerView(context, attrs, defStyleAttr) {

    override fun onTouchEvent(e: MotionEvent?) = performClick()
    override fun performClick() = super.performClick().also { return false }
}

