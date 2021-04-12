package com.louis.app.cavity.ui.history.adapter

import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView

class HistoryDivider(@Px private val height: Int, @ColorInt private val dividerColor: Int) :
    RecyclerView.ItemDecoration() {

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = dividerColor
        style = Paint.Style.STROKE
        strokeWidth = height.toFloat()
    }

    private fun isDecorated(previousChild: View, child: View, parent: RecyclerView): Boolean {
        val previousViewHolder = parent.getChildViewHolder(previousChild)
        val viewHolder = parent.getChildViewHolder(child)

        return previousViewHolder !is HistoryRecyclerAdapter.HistorySeparatorViewHolder &&
            viewHolder !is HistoryRecyclerAdapter.HistorySeparatorViewHolder
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val points = mutableListOf<Float>()
        var previousChild: View? = null

        parent.forEach {
            if (parent.getChildAdapterPosition(it) < state.itemCount - 1 && previousChild != null) {
                if (isDecorated(previousChild!!, it, parent)) {
                    val top = it.top.toFloat()
                    points.apply {
                        add(it.left.toFloat())
                        add(top)
                        add(it.right.toFloat())
                        add(top)
                    }
                }
            }

            previousChild = it
        }

        c.drawLines(points.toFloatArray(), dividerPaint)
    }
}
