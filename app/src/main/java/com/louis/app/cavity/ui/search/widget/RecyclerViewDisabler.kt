package com.louis.app.cavity.ui.search.widget

import android.view.MotionEvent

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener

class RecyclerViewDisabler(private val onDisableRecyclerTouch: () -> Unit) : OnItemTouchListener {
    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent) = true

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        if (e.action == MotionEvent.ACTION_UP) {
            onDisableRecyclerTouch()
        }
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}
