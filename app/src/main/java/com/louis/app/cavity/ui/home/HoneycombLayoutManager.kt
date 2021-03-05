package com.louis.app.cavity.ui.home

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.util.L
import kotlin.math.roundToInt

/**
 * Organize the views in a honeycomb fashion
 * Even rows will contain rowCount items , odd rows will contain rowCount - 1 items
 * Thus rowCount must be at least 1
 *
 * Thinked to be used with HexagonalView
 */
class HoneycombLayoutManager(
    context: Context,
    private val colCount: Int,
    private val orientation: Int
) :
    RecyclerView.LayoutManager() {

    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1

        // 3/4 is the pointy part ratio (compared to its bounds length) of the hexagon
        const val OVERLAPING_FACTOR = 0.75
    }

    // A group is a row with its child, thiner, row
    private val groupItemCount = (2 * colCount) - 1

    private var scrollOffset = 0

//    // Since all items will have same width, we can cache this
//    private var childRowOffset = 0

    private fun fill(recycler: RecyclerView.Recycler, adapterItemCount: Int) {
        detachAndScrapAttachedViews(recycler)

        for (i in 0 until adapterItemCount) {
            L.v("____________________________________________________")
            L.v("POSITION : $i")
            L.v("isItemInChildRow: ${isItemInChildRow(position = i)}")
            L.v("positionInRow: ${getPositionInRow(position = i)}")
            L.v("isItemOnTopFirstRow:  ${isItemInTopFirstRow(i)}")
            L.v("rowNumber:  ${getRowNumberForItem(i)}")

            val view = recycler.getViewForPosition(i)
            addView(view)

            if (orientation == VERTICAL) {
                measureChild(view, width / colCount, 0)
            }

//            if (orientation == HORIZONTAL) {
//                measureChild(view, 0, height / longRowColsCount)
//            }

            val top =
                if (isItemInTopFirstRow(i)) 0 - scrollOffset else {
                    ((getRowNumberForItem(i) * (view.measuredHeight * OVERLAPING_FACTOR)) - scrollOffset).roundToInt()
                }

            if (isItemInChildRow(i)) {
                val childRowOffset = view.measuredWidth / 2
                val left = childRowOffset + view.measuredWidth * getPositionInRow(i)
                val right = left + view.measuredWidth
                val bottom = top + view.measuredHeight

                layoutDecorated(view, left, top, right, bottom)
                logPosition(view, position = i)
            } else {
                val left = getPositionInRow(i) * view.measuredWidth // i * getPositionInRow ?
                val right = left + view.measuredWidth
                val bottom = top + view.measuredHeight


                layoutDecorated(view, left, top, right, bottom)
                logPosition(view, position = i)
            }
        }
    }

    private fun isItemInChildRow(position: Int): Boolean {
        val threshold = colCount - 1
        return position % groupItemCount > threshold
    }

    // Might be useless if we better compute "top" value
    private fun isItemInTopFirstRow(position: Int) = position <= colCount - 1

    // Only works when longRowCols = 2 for now
    private fun getPositionInRow(position: Int): Int {
        return if (isItemInChildRow(position)) {
            position % groupItemCount - colCount
        } else {
            position % groupItemCount
        }
    }

    private fun getRowNumberForItem(position: Int): Int {
        var groupParentRowPosition = (position / groupItemCount) * 2
        if (isItemInChildRow(position)) {
            groupParentRowPosition += 1
        }

        return groupParentRowPosition
    }

    private fun doOnScroll(
        d: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        scrollOffset += d
        fill(recycler, state.itemCount)
        return d
    }

    private fun logPosition(view: View, position: Int) {
        L.v("View at position $position is set to: ${view.left}, ${view.top}")
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.itemCount > 0) {
            fill(recycler, state.itemCount)
        }
    }

    override fun canScrollHorizontally() = orientation == HORIZONTAL

    override fun canScrollVertically() = orientation == VERTICAL

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) =
        if (orientation == VERTICAL) 0 else doOnScroll(dx, recycler, state)

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) =
        if (orientation == HORIZONTAL) 0 else doOnScroll(dy, recycler, state)

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }
}
