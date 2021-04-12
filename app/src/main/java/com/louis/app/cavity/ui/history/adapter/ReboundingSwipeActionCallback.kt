package com.louis.app.cavity.ui.history.adapter

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.ln

class ReboundingSwipeActionCallback : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT
) {

    companion object {
        private const val swipeReboundingElasticity = 0.8f
        private const val trueSwipeThreshold = 0.4f
    }

    interface ReboundableViewHolder {
        val reboundableView: View

        fun onRebounded()

        fun onReboundOffsetChanged(
            currentSwipePercentage: Float,
            swipeThreshold: Float,
            currentTargetHasMetThresholdOnce: Boolean
        )
    }

    private var currentTargetPosition: Int = -1
    private var currentTargetHasMetThresholdOnce: Boolean = false

    // Never dismiss item
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = Float.MAX_VALUE

    // Never dismiss item
    override fun getSwipeVelocityThreshold(defaultValue: Float) = Float.MAX_VALUE

    // Never dismiss item
    override fun getSwipeEscapeVelocity(defaultValue: Float) = Float.MAX_VALUE

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ):
        Boolean = false

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (currentTargetHasMetThresholdOnce && viewHolder is ReboundableViewHolder) {
            currentTargetHasMetThresholdOnce = false
            viewHolder.onRebounded()
        }

        super.clearView(recyclerView, viewHolder)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (viewHolder !is ReboundableViewHolder) return

        if (currentTargetPosition != viewHolder.adapterPosition) {
            currentTargetPosition = viewHolder.adapterPosition
            currentTargetHasMetThresholdOnce = false
        }

        val itemView = viewHolder.itemView
        val currentSwipePercentage = abs(dX) / itemView.width

        viewHolder.onReboundOffsetChanged(
            currentSwipePercentage,
            trueSwipeThreshold,
            currentTargetHasMetThresholdOnce
        )

        translateReboundingView(itemView, viewHolder, dX)

        if (currentSwipePercentage >= trueSwipeThreshold && !currentTargetHasMetThresholdOnce) {
            currentTargetHasMetThresholdOnce = true
        }
    }

    private fun translateReboundingView(
        itemView: View,
        viewHolder: ReboundableViewHolder,
        dX: Float
    ) {
        val swipeDismissDistanceHorizontal = itemView.width * trueSwipeThreshold
        val dragFraction =
            ln((1 - (dX / swipeDismissDistanceHorizontal)).toDouble()) / ln(3.toDouble())

        val dragTo = dragFraction * swipeDismissDistanceHorizontal * swipeReboundingElasticity

        viewHolder.reboundableView.translationX = -dragTo.toFloat()
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }
}
