package com.louis.app.cavity.ui.history.adapter

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

class JumpSmoothScroller(
    context: Context,
    private val jumpThreshold: Int
) :
    LinearSmoothScroller(context) {

    override fun getVerticalSnapPreference() = SNAP_TO_START

    override fun getHorizontalSnapPreference() = SNAP_TO_START

    override fun onSeekTargetStep(dx: Int, dy: Int, state: RecyclerView.State, action: Action) {
        val layoutManager = layoutManager as? LinearLayoutManager
        if (layoutManager != null) {
            if (targetPosition + jumpThreshold < layoutManager.findFirstVisibleItemPosition()) {
                action.jumpTo(targetPosition + jumpThreshold)
                return
            }
            if (targetPosition - jumpThreshold > layoutManager.findLastVisibleItemPosition()) {
                action.jumpTo(targetPosition - jumpThreshold)
                return
            }
        }

        super.onSeekTargetStep(dx, dy, state, action)
    }
}

