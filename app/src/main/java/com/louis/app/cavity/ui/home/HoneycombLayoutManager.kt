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
    @Orientation orientation: Int
) :
    RecyclerView.LayoutManager() {

    companion object {
        @IntDef(HORIZONTAL, VERTICAL)
        annotation class Orientation

        private const val HORIZONTAL = 0
        private const val VERTICAL = 1
    }


    private fun fillBottom(recycler: RecyclerView.Recycler, adapterItemCount: Int) {
        var top: Int
        val startPosition: Int

        if (childCount > 0) {
            val lastChild = getChildAt(childCount - 1)!!
            val lastChildPosition = getPosition(lastChild)
            startPosition = lastChildPosition + 1
            val lp = lastChild.layoutParams as RecyclerView.LayoutParams // ?
            top = getDecoratedBottom(lastChild) + lp.bottomMargin
        }
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        detachAndScrapAttachedViews(recycler)

        if (state.itemCount > 0) {
            fillBottom(recycler, state.itemCount)
        }
    }
}
