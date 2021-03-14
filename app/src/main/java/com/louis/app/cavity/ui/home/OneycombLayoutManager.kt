package com.louis.app.cavity.ui.home

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.OrientationHelper.createHorizontalHelper
import androidx.recyclerview.widget.OrientationHelper.createVerticalHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.louis.app.cavity.util.L
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Organize the views in a honeycomb fashion
 * Even rows will contain rowCount items, odd rows will contain rowCount - 1 items
 * Thus rowCount must be at least 1
 *
 * Thinked to be used with HexagonalView
 */
class OneycombLayoutManager(
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

    private val orientationHelper =
        if (orientation == VERTICAL) createVerticalHelper(this)
        else createHorizontalHelper(this)

    private val renderState = RenderState(colCount)

//    // Since all items will have same width, we can cache this
//    private var childRowOffset = 0

    // TODO: implement onDetachedFromWindow to make the views avalaible for the view pool, since this recycler view will share his viewpool in the future

    private fun fillBottom(recycler: RecyclerView.Recycler, adapterItemCount: Int): Int {
        val xstart = renderState.toFill

        if (renderState.freeScroll != RenderState.FREE_SCROLL_NAN) {
            recycleViewsFromStart(recycler, renderState.freeScroll)
        }

        while (renderState.toFill > 0 && renderState.hasMore(adapterItemCount)) {
            val view = renderState.next(recycler)
            val isInChildRow = renderState.isItemInChildRow()
            val positionInRow = renderState.getPositionInRow(isInChildRow)

            addView(view)

            if (orientation == VERTICAL) {
                measureChild(view, width / colCount, 0)
            } else if (orientation == HORIZONTAL) {
                measureChild(view, 0, height / colCount)
            }

            val start = renderState.getStartForPosition(view, isInChildRow) // rename to top ?
//            val consumed = orientationHelper.getDecoratedMeasurement(view)

            if (isInChildRow) {
                val childRowOffset = view.measuredWidth / 2
                val left = childRowOffset + view.measuredWidth * positionInRow
                val right = left + view.measuredWidth
                val bottom = start + view.measuredHeight

                L.v("position: ${renderState.currentPosition}, start: $start, left: $left")
                layoutDecorated(view, left, start, right, bottom)

                /*     if (isRowCompleted(positionInRow, isInChildRow = true)) {
                         L.v("Update toFill for postiton ${renderState.currentPosition}")
                         // TODO: Take into account margin
                         val consumed = (view.measuredHeight * OVERLAPING_FACTOR).roundToInt()

                         onRowCompleted(consumed)
                     }*/
            } else {
                val left = positionInRow * view.measuredWidth
                val right = left + view.measuredWidth
                val bottom = start + view.measuredHeight

                L.v("position: ${renderState.currentPosition}, start: $start, left: $left")
                layoutDecorated(view, left, start, right, bottom)

                /*    if (isRowCompleted(positionInRow, isInChildRow = false)) {
                        L.v("Update toFill for postiton: ${renderState.currentPosition}")
                        val consumed = (view.measuredHeight * OVERLAPING_FACTOR).roundToInt()

                        onRowCompleted(consumed)
                    }*/
            }

            if (isRowCompleted(positionInRow, isInChildRow)) {
                L.v("Update toFill for postiton ${renderState.currentPosition}")
                // TODO: Take into account margin
                val consumed = (view.measuredHeight * OVERLAPING_FACTOR).roundToInt()

                onRowCompleted(recycler, consumed)
            }

            renderState.currentPosition++
        }

        return xstart - renderState.toFill
//        L.v("Layout $i childrens")
    }

    private fun fillTop(recycler: RecyclerView.Recycler, adapterItemCount: Int): Int {
        val xstart = renderState.toFill

        if (renderState.freeScroll != RenderState.FREE_SCROLL_NAN) {
            recycleViewsFromStart(recycler, renderState.freeScroll)
        }

        while (renderState.toFill > 0 && renderState.hasMore(adapterItemCount)) {
            val view = renderState.next(recycler)
            val isInChildRow = renderState.isItemInChildRow()
            val positionInRow = renderState.getPositionInRow(isInChildRow)

            addView(view, 0)

            if (orientation == VERTICAL) {
                measureChild(view, width / colCount, 0)
            } else if (orientation == HORIZONTAL) {
                measureChild(view, 0, height / colCount)
            }

            val start = -renderState.getStartForPosition(view, isInChildRow) // rename to top ?
//            val consumed = orientationHelper.getDecoratedMeasurement(view)

            if (isInChildRow) {
                val childRowOffset = view.measuredWidth / 2
                val left = childRowOffset + view.measuredWidth * positionInRow
                val right = left + view.measuredWidth
                val bottom = start - view.measuredHeight

                L.v("position: ${renderState.currentPosition}, start: $start, left: $left")
                layoutDecorated(view, left, start, right, bottom)

                /*     if (isRowCompleted(positionInRow, isInChildRow = true)) {
                         L.v("Update toFill for postiton ${renderState.currentPosition}")
                         // TODO: Take into account margin
                         val consumed = (view.measuredHeight * OVERLAPING_FACTOR).roundToInt()

                         onRowCompleted(consumed)
                     }*/
            } else {
                val left = positionInRow * view.measuredWidth
                val right = left + view.measuredWidth
                val bottom = start - view.measuredHeight

                L.v("position: ${renderState.currentPosition}, start: $start, left: $left")
                layoutDecorated(view, left, start, right, bottom)

                /*    if (isRowCompleted(positionInRow, isInChildRow = false)) {
                        L.v("Update toFill for postiton: ${renderState.currentPosition}")
                        val consumed = (view.measuredHeight * OVERLAPING_FACTOR).roundToInt()

                        onRowCompleted(consumed)
                    }*/
            }

            if (isRowCompleted(positionInRow, isInChildRow)) {
                L.v("Update toFill for postiton ${renderState.currentPosition}")
                // TODO: Take into account margin
                val consumed = (view.measuredHeight * OVERLAPING_FACTOR).roundToInt()

                onRowCompleted(recycler, consumed)
            }

            renderState.currentPosition++
        }

        return xstart - renderState.toFill
//        L.v("Layout $i childrens")
    }

    private fun doOnScroll(
        d: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (childCount == 0) {
            return 0
        }

        val layoutDirection = if (d > 0) RenderState.LAYOUT_TO_END else RenderState.LAYOUT_TO_START
        updateRenderState(layoutDirection, abs(d), canUseExistingSpace = true)

        val filled =
            if (d > 0) fillBottom(
                recycler,
                state.itemCount
            ) else return 0//fillTop(recycler, state.itemCount)
        val consumed = renderState.freeScroll + filled
        val scrolled = if (abs(d) > consumed) layoutDirection * consumed else d
        orientationHelper.offsetChildren(-scrolled)
        renderState.scrollOffset += scrolled

        return scrolled

    }

    private fun logPosition(view: View, position: Int) {
        L.v("View at position $position is set to: ${view.left}, ${view.top}")
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        L.v("OnLayout (isPrelayout: ${state.isPreLayout})")

        renderState.offset = orientationHelper.startAfterPadding
        renderState.toFill = orientationHelper.endAfterPadding - renderState.offset

        if (childCount > 0) {
            val referenceChild = getChildAt(0)!!
            renderState.currentPosition = getPosition(referenceChild)
        } else {
            renderState.currentPosition = 0
        }

        if (!renderState.anchor.valid && state.itemCount > 0) {
            renderState.anchor.valid = true
            // TODO: check if this works after scrolling is ok
            val view = findReferenceChild(recycler, state, 0, childCount, state.itemCount)

            if (view == null) {
                renderState.anchor.coordinate = orientationHelper.startAfterPadding
            } else {
                renderState.currentPosition = getPosition(view)
                L.v("currentPosition: ${renderState.currentPosition}")
                renderState.anchor.coordinate = orientationHelper.getDecoratedStart(view)
            }

            // Might be necessary to handle some (apparently) edge cases, see rv.png
        }

        detachAndScrapAttachedViews(recycler)

        renderState.freeScroll = RenderState.FREE_SCROLL_NAN
        renderState.layoutDirection = RenderState.LAYOUT_TO_END

        if (state.itemCount > 0) {
            fillBottom(recycler, state.itemCount)
        }

        //removeAndRecycleAllViews(recycler) should be removeAndRecycleScrap here but nos sure if we had to do this
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

    private fun findReferenceChild(
        recycler: Recycler?,
        state: RecyclerView.State?,
        start: Int,
        end: Int,
        itemCount: Int
    ): View? {
        var invalidMatch: View? = null
        var outOfBoundsMatch: View? = null
        val boundsStart: Int = orientationHelper.startAfterPadding
        val boundsEnd: Int = orientationHelper.endAfterPadding
        val diff = if (end > start) 1 else -1
        var i = start
        while (i != end) {
            val view = getChildAt(i)
            val position = getPosition(view!!)
            if (position in 0 until itemCount) {
                if ((view.layoutParams as RecyclerView.LayoutParams).isItemRemoved) {
                    if (invalidMatch == null) {
                        invalidMatch = view // removed item, least preferred
                    }
                } else if (
                    orientationHelper.getDecoratedStart(view) >= boundsEnd ||
                    orientationHelper.getDecoratedEnd(view) < boundsStart
                ) {
                    if (outOfBoundsMatch == null) {
                        outOfBoundsMatch = view // item is not visible, less preferred
                    }
                } else {
                    return view
                }
            }
            i += diff
        }
        return outOfBoundsMatch ?: invalidMatch
    }

    private fun recycleByRenderState(recycler: Recycler) {
        if (renderState.layoutDirection == RenderState.LAYOUT_TO_START) {
            recycleViewsFromEnd(recycler, renderState.freeScroll)
        } else {
            recycleViewsFromStart(recycler, renderState.freeScroll)
        }
    }

    private fun recycleViewsFromStart(recycler: Recycler, dt: Int) {
        if (dt < 0) {
            L.v(
                "Called recycle from start with a negative value. This might happen"
                        + " during layout changes but may be sign of a bug"
            )
            return
        }
        val limit: Int = orientationHelper.startAfterPadding + dt
        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (orientationHelper.getDecoratedEnd(child) > limit) { // stop here
                recycleChildren(recycler, 0, i)
                return
            }
        }
    }

    private fun recycleViewsFromEnd(recycler: Recycler, dt: Int) {
        val childCount = childCount
        if (dt < 0) {
            L.v(
                "Called recycle from end with a negative value. This might happen" +
                        " during layout changes but may be sign of a bug"
            )
            return
        }
        val limit: Int = orientationHelper.endAfterPadding - dt
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i)
            if (orientationHelper.getDecoratedStart(child) < limit) { // stop here
                recycleChildren(recycler, childCount - 1, i)
                return
            }
        }
    }

    private fun recycleChildren(recycler: Recycler, startIndex: Int, endIndex: Int) {
        if (startIndex == endIndex) {
            return
        }
        L.v("Recycling " + abs(startIndex - endIndex) + " items")
        if (endIndex > startIndex) {
            for (i in endIndex - 1 downTo startIndex) {
                removeAndRecycleViewAt(i, recycler)
            }
        } else {
            for (i in startIndex downTo endIndex + 1) {
                removeAndRecycleViewAt(i, recycler)
            }
        }
    }

    private fun onRowCompleted(recycler: Recycler, consumed: Int) {
        with(renderState) {
            renderState.toFill -= consumed

            if (freeScroll != RenderState.FREE_SCROLL_NAN) {
                freeScroll += consumed

                if (toFill < 0) {
                    freeScroll += toFill
                }

                recycleByRenderState(recycler)
            }
        }
    }

    private fun isRowCompleted(positionInRow: Int, isInChildRow: Boolean): Boolean {
        val limit = if (isInChildRow) colCount - 1 else colCount
        // +1 to get an non zero based index
        return positionInRow + 1 == limit
    }

    private data class Anchor(var coordinate: Int, var valid: Boolean)

    private fun updateRenderState(
        layoutDirection: Int,
        requiredSpace: Int,
        canUseExistingSpace: Boolean
    ) {
        renderState.layoutDirection = layoutDirection

        if (layoutDirection == RenderState.LAYOUT_TO_END) {
            val view = getChildAt(itemCount - 1)

            view?.let {
                with(renderState) {
                    currentPosition = getPosition(it)
                    offset = orientationHelper.getDecoratedEnd(it)
                    freeScroll =
                        orientationHelper.getDecoratedEnd(it) - orientationHelper.endAfterPadding
                }
            }
        } else {
            val view = getChildAt(0)
            view?.let {
                with(renderState) {
                    currentPosition = getPosition(it)
                    offset = orientationHelper.getDecoratedStart(it)
                    freeScroll =
                        -orientationHelper.getDecoratedStart(it) + orientationHelper.startAfterPadding
                }
            }
        }

        renderState.toFill = requiredSpace
        if (canUseExistingSpace) {
            renderState.toFill -= renderState.freeScroll
        }
    }

    private class RenderState(private val colCount: Int) {
        companion object {
            const val FREE_SCROLL_NAN = Integer.MIN_VALUE
            const val LAYOUT_TO_START = -1
            const val LAYOUT_TO_END = 1
        }

        val groupItemCount = (2 * colCount) - 1
        val anchor = Anchor(coordinate = 0, valid = false)
        var currentPosition = 0 // current adapter postion
        var offset = 0  // Position where we should begin layout views
        var scrollOffset = 0 // Children views offset due to scroll position
        var freeScroll = 0 // Amount of space that we can scroll without creating a new view
        var toFill = 0 // Distance of available space to fill with views
        var layoutDirection = LAYOUT_TO_END // Direction in which we should fill the layout

        fun hasMore(adapterItemCount: Int) =
            currentPosition in 0 until adapterItemCount

        fun next(recycler: Recycler) = recycler.getViewForPosition(currentPosition)

        fun isItemInChildRow(): Boolean {
            val threshold = colCount - 1
            return currentPosition % groupItemCount > threshold
        }

        // Might be useless if we better compute "top" value
        fun isItemInTopFirstRow() = currentPosition <= colCount - 1

        fun getPositionInRow(childRow: Boolean): Int {
            return if (childRow) {
                currentPosition % groupItemCount - colCount
            } else {
                currentPosition % groupItemCount
            }
        }

        fun getRowNumberForItem(isInChildRow: Boolean): Int {
            var groupParentRowPosition = (currentPosition / groupItemCount) * 2
            if (isInChildRow) {
                groupParentRowPosition += 1
            }

            return groupParentRowPosition
        }

        fun getStartForPosition(view: View, isInChildRow: Boolean): Int {
            return if (isItemInTopFirstRow()) {
                0 - scrollOffset
            } else {
                val start = view.measuredHeight * OVERLAPING_FACTOR
                val rowNumber = getRowNumberForItem(isInChildRow)
                L.v("rowNumber: $rowNumber, start: $start")
                ((rowNumber * start) - scrollOffset).roundToInt()
            }.also { L.v("getStartForPosition $currentPosition : $it") }
        }
    }
}

