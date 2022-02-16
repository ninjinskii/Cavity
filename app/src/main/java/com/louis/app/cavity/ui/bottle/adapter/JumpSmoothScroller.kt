package com.louis.app.cavity.ui.bottle.adapter

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
            val first = layoutManager.findFirstVisibleItemPosition()
            val last = layoutManager.findLastVisibleItemPosition()

            when {
                targetPosition + jumpThreshold < first -> {
                    action.jumpTo(targetPosition + jumpThreshold)
                    return
                }
                targetPosition - jumpThreshold > last -> {
                    action.jumpTo(targetPosition - jumpThreshold)
                    return
                }

            }
        }

        super.onSeekTargetStep(dx, dy, state, action)
    }
}

