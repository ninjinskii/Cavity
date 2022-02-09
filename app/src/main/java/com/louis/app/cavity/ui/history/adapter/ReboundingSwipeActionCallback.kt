package com.louis.app.cavity.ui.history.adapter

import android.graphics.Canvas
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.roundToInt

class ReboundingSwipeActionCallback : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT
) {

    companion object {
        private const val SWIPE_REBOUNDING_ELASTICITY = 0.8f
    }

    interface ReboundableViewHolder {
        val reboundableView: View

        fun onRebounded(position: Int)

        fun onReboundOffsetChanged(
            currentSwipeDistance: Int,
            swipeThreshold: Int,
            currentTargetHasMetThresholdOnce: Boolean
        )
    }

    @Px
    private var trueSwipeThreshold: Int = -1
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
            viewHolder.onRebounded(viewHolder.absoluteAdapterPosition)
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


        if (currentTargetPosition != viewHolder.bindingAdapterPosition) {
            currentTargetPosition = viewHolder.bindingAdapterPosition
            currentTargetHasMetThresholdOnce = false
        }

        val currentSwipeDistance = abs(dX).roundToInt()
        val itemView = viewHolder.itemView
        trueSwipeThreshold =
            itemView.resources.getDimensionPixelSize(R.dimen.history_swipe_threshold)

        viewHolder.onReboundOffsetChanged(
            currentSwipeDistance,
            trueSwipeThreshold,
            currentTargetHasMetThresholdOnce
        )

        translateReboundingView(viewHolder, dX)

        if (currentSwipeDistance >= trueSwipeThreshold && !currentTargetHasMetThresholdOnce) {
            currentTargetHasMetThresholdOnce = true
        }
    }

    private fun translateReboundingView(
        viewHolder: ReboundableViewHolder,
        dX: Float
    ) {
        val dragFraction =
            ln((1 - (dX / trueSwipeThreshold)).toDouble()) / ln(3.toDouble())

        val dragTo = dragFraction * trueSwipeThreshold * SWIPE_REBOUNDING_ELASTICITY

        viewHolder.reboundableView.translationX = -dragTo.toFloat()
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }
}
