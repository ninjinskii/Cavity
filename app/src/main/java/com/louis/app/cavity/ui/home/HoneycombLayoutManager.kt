package com.louis.app.cavity.ui.home

import android.content.Context
import androidx.annotation.IntDef
import androidx.recyclerview.widget.RecyclerView

/**
 * Organize the views in a honeycomb fashion
 * Even rows will contain longRowColsCount items
 * Odd rows will contain longRowColsCount - 1 items
 *
 * Thinked to be used with HexagonalView
 */
class HoneycombLayoutManager(
    context: Context,
    longRowColsCount: Int,
    @Orientation private val orientation: Int
) :
    RecyclerView.LayoutManager() {

    var scrollOffset = 0

    companion object {
        @IntDef(HORIZONTAL, VERTICAL)
        annotation class Orientation

        private const val HORIZONTAL = 0
        private const val VERTICAL = 1
    }

    private fun fill(recycler: RecyclerView.Recycler, adapterItemCount: Int) {

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
