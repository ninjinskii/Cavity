package com.louis.app.cavity.ui.home

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.util.L

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
    }

    // Number of items to display in an even row + its child row
    private val rowCoupleItemCount = (2 * longRowColsCount) - 1
    private var scrollOffset = 0

    private fun fill(recycler: RecyclerView.Recycler, adapterItemCount: Int) {
        detachAndScrapAttachedViews(recycler)

        for (i in 0 until 3) {
            val view = recycler.getViewForPosition(i)
            addView(view)

            if (orientation == VERTICAL) {
                L.v("layout manager width: $width")
                measureChild(view, width / longRowColsCount, 0)
                L.v("measuredChildHeight: ${view.measuredHeight}")
            }

//            if (orientation == HORIZONTAL) {
//                measureChild(view, 0, height / longRowColsCount)
//            }

//            if (isItemInChildRow(i)) {
//                val left = 0
//            } else {
            val left = i * view.measuredWidth
            val right = left + view.measuredWidth
            L.v("$left")
            val top = 0
            layoutDecorated(view, left, top, right, view.measuredHeight)
//            }


        }
    }

    private fun isItemInChildRow(position: Int) =
        position % rowCoupleItemCount !in 0..longRowColsCount

    private fun doOnScroll(
        d: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        scrollOffset += d
        fill(recycler, state.itemCount)
        return d
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
        doOnScroll(dx, recycler, state)

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) =
        doOnScroll(dy, recycler, state)

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }
}
