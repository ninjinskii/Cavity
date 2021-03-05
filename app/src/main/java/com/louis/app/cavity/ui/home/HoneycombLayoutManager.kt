package com.louis.app.cavity.ui.home

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.util.L
import kotlin.math.roundToInt

/**
 * Organize the views in a honeycomb fashion
 * Even rows will contain longRowColsCount items
 * Odd rows will contain longRowColsCount - 1 items
 *
 * Thinked to be used with HexagonalView
 */
class HoneycombLayoutManager(
    context: Context,
    private val longRowColsCount: Int,
    private val orientation: Int
) :
    RecyclerView.LayoutManager() {

    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1

        // 3/4 is the pointy bottom of hexagon
        const val OVERLAPING_FACTOR = 0.75
    }

    // Number of items to display in an even row + its child row
    private val rowCoupleItemCount = (2 * longRowColsCount) - 1

    init {
        L.v("test:")

        for (i in 0 until 10) {
//            L.v("${i % rowCoupleItemCount}")

            L.v("${isItemInChildRow(position = i)}")
        }
    }

    private var scrollOffset = 0

//    // Since all items will have same width, we can cache this
//    private var childRowOffset = 0

    private fun fill(recycler: RecyclerView.Recycler, adapterItemCount: Int) {
        detachAndScrapAttachedViews(recycler)

        for (i in 0 until 4) {
            val view = recycler.getViewForPosition(i)
            addView(view)

            if (orientation == VERTICAL) {
                measureChild(view, width / longRowColsCount, 0)
            }

//            if (orientation == HORIZONTAL) {
//                measureChild(view, 0, height / longRowColsCount)
//            }

            val top =
                if (isItemInTopFisrtRow(i)) 0 - scrollOffset else (((i - longRowColsCount + 1) * (view.measuredHeight * OVERLAPING_FACTOR)) - scrollOffset).roundToInt()

            L.v("isItemOnTopFirstRow for pos $i:  ${isItemInTopFisrtRow(i)}")
            if (isItemInChildRow(i)) {
//                L.v("is in long row")
                val childRowOffset = view.measuredWidth / 2
                val left = childRowOffset
                val right = left + view.measuredWidth
                val bottom = top + view.measuredHeight

                layoutDecorated(view, left, top, right, bottom)
                logPosition(view, position = i)
            } else {
//                L.v("is in child row")
                val left = i * view.measuredWidth
                val right = left + view.measuredWidth
                val bottom = top + view.measuredHeight


                layoutDecorated(view, left, top, right, bottom)
                logPosition(view, position = i)
            }
        }
    }

    private fun isItemInChildRow(position: Int) =
        position % rowCoupleItemCount == longRowColsCount

    private fun isItemInTopFisrtRow(position: Int) = position <= longRowColsCount - 1

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
