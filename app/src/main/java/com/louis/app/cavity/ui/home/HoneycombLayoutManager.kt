package com.louis.app.cavity.ui.home

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.OrientationHelper.createHorizontalHelper
import androidx.recyclerview.widget.OrientationHelper.createVerticalHelper
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.util.L
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Organize the views in a honeycomb fashion
 * Even rows will contain rowCount items, odd rows will contain rowCount - 1 items
 * Thus rowCount must be at least 2
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
        private const val OVERLAPING_FACTOR = 0.25
    }

    private val groupItemCount = (2 * colCount) - 1
    private val oHelper =
        if (orientation == VERTICAL) createVerticalHelper(this)
        else createHorizontalHelper(this)

    private var anchorPosition = 0
    private var anchorOffset = 0

    init {
        if (colCount < 2) {
            throw IllegalArgumentException("Honeycomb layout manager require at least two rows.")
        }
    }

    // TODO: share viewpool
    // TODO: Predictive animations: see State.itemCount docs

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        L.v("OnLayout (isPrelayout: ${state.isPreLayout})")

        detachAndScrapAttachedViews(recycler)

        if (state.itemCount > 0) {
            fillTowardsEnd(recycler)
        }
    }

    private fun fillTowardsEnd(recycler: RecyclerView.Recycler) {
        val toFill = oHelper.endAfterPadding
        var filled: Int // Might be necessary to better compute actual scrolled distance in doOnScroll()
        val startPos: Int
        var top: Int

        if (childCount > 0) {
            val lastChild = getChildAt(childCount - 1)!!
            val lastChildPos = getPosition(lastChild)
            startPos = lastChildPos + 1
            top =
                oHelper.getDecoratedEnd(lastChild) - (lastChild.measuredHeight apply OVERLAPING_FACTOR)
            filled = top
        } else {
            startPos = 0
            filled = 0
            top = oHelper.startAfterPadding
        }

        for (i in startPos until itemCount) {
            if (top > toFill) break

            val view = recycler.getViewForPosition(i)

            addView(view)

            if (orientation == VERTICAL) {
                measureChild(view, width - (width / colCount), 0)
            } else if (orientation == HORIZONTAL) {
                measureChild(view, 0, height - (height / colCount))
            }

//            val isInChildRow = isItemInChildRow(i)
//            val positionInRow = getPositionInRow(i, isInChildRow)
//            val bottom: Int
//
//            if (isInChildRow) {
//                val childRowOffset = view.measuredWidth / 2
//                bottom = top + view.measuredHeight
//                val left = childRowOffset + view.measuredWidth * positionInRow
//                val right = left + view.measuredWidth
//
//                layoutDecoratedWithMargins(view, left, top, right, bottom)
//            } else {
//                bottom = top + view.measuredHeight
//                val left = view.measuredWidth * positionInRow
//                val right = left + view.measuredWidth
//
//                layoutDecoratedWithMargins(view, left, top, right, bottom)
//            }
//
//            if (isRowCompleted(positionInRow, isInChildRow, reverse = false)) {
//                top = bottom - (view.measuredHeight apply OVERLAPING_FACTOR)
//                filled += view.measuredHeight apply OVERLAPING_FACTOR
//            }

            top += layoutTowardsEndVertically(top, view, i)
        }

        L.v("childCount : $childCount")
    }

    private fun layoutTowardsEndVertically(start: Int, view: View, i: Int) : Int {
        val isInChildRow = isItemInChildRow(i)
        val positionInRow = getPositionInRow(i, isInChildRow)
        val bottom: Int

        if (isInChildRow) {
            val childRowOffset = view.measuredWidth / 2
            bottom = start + view.measuredHeight
            val left = childRowOffset + view.measuredWidth * positionInRow
            val right = left + view.measuredWidth

            if (orientation == VERTICAL) {
                layoutDecoratedWithMargins(view, left, start, right, bottom)
            } else {
                layoutDecoratedWithMargins(view, bottom, left, start, right)
            }
        } else {
            bottom = start + view.measuredHeight
            val left = view.measuredWidth * positionInRow
            val right = left + view.measuredWidth

            if (orientation == VERTICAL) {
                layoutDecoratedWithMargins(view, left, start, right, bottom)
            } else {
                layoutDecoratedWithMargins(view, bottom, left, start, right)
            }
        }

        if (isRowCompleted(positionInRow, isInChildRow, reverse = false)) {
            return bottom - (view.measuredHeight apply OVERLAPING_FACTOR)
        }

        return 0
    }

    private fun layoutTowardsEndHorizontally(start: Int, view: View, i: Int) {
//        val isInChildRow = isItemInChildRow(i)
//        val positionInRow = getPositionInRow(i, isInChildRow)
//        val end: Int
//
//        if (isInChildRow) {
//            val childRowOffset = view.measuredHeight / 2
//            end = start + if (orientation == VERTICAL) view.measuredHeight else view.measuredWidth
//            val left =
//        }
    }

    private fun fillTowardsStart(recycler: RecyclerView.Recycler) {
        var bottom: Int

        if (childCount == 0) return

        val firstChild = getChildAt(0)!!
        val firstChildPos = getPosition(firstChild)
        var filled = firstChild.top

        if (firstChildPos == 0) return

        val toFill = if (clipToPadding) paddingTop else 0
        bottom =
            oHelper.getDecoratedStart(firstChild) + (firstChild.measuredHeight apply OVERLAPING_FACTOR)

        for (i in firstChildPos - 1 downTo 0) {
            if (bottom < toFill) break

            val view = recycler.getViewForPosition(i)
            addView(view, 0)

            anchorPosition--

            if (orientation == VERTICAL) {
                measureChild(view, width - (width / colCount), 0)
            } else if (orientation == HORIZONTAL) {
                measureChild(view, 0, height - (height / colCount))
            }

            val top = bottom - view.measuredHeight
            val isInChildRow = isItemInChildRow(i)
            val positionInRow = getPositionInRow(i, isInChildRow)

            if (isInChildRow) {
                val childRowOffset = view.measuredWidth / 2
                val left = childRowOffset + view.measuredWidth * positionInRow
                val right = left + view.measuredWidth

                layoutDecoratedWithMargins(view, left, top, right, bottom)
            } else {
                val left = view.measuredWidth * positionInRow
                val right = left + view.measuredWidth

                layoutDecoratedWithMargins(view, left, top, right, bottom)
            }

            if (isRowCompleted(positionInRow, isInChildRow, reverse = true)) {
                bottom = top + (firstChild.measuredHeight apply OVERLAPING_FACTOR)
                filled += view.measuredHeight
            }
        }

        L.v("childCount : $childCount")
    }

    private fun doOnScroll(
        d: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        return when {
            childCount == 0 -> 0
            d < 0 -> {
                val toFill = oHelper.startAfterPadding
                var scrolled = 0

                while (scrolled > d) {
                    val firstChild = getChildAt(0)!!
                    val firstChildTop = oHelper.getDecoratedStart(firstChild)
                    val hangingTop = max(0, toFill - firstChildTop)

                    val scrollBy = min(hangingTop, scrolled - d)
                    oHelper.offsetChildren(scrollBy)
                    scrolled -= scrollBy
                    if (anchorPosition == 0) break
                    fillTowardsStart(recycler)
                }
                scrolled
            }
            d > 0 -> {
                val toFill = oHelper.endAfterPadding
                var scrolled = 0

                while (scrolled < d) {
                    val lastChild = getChildAt(childCount - 1)!!
                    val lastChildPosition = getPosition(lastChild)
                    val lastChildBottom = oHelper.getDecoratedEnd(lastChild)
                    val hangingBottom = max(0, lastChildBottom - toFill)
                    val scrollBy = min(hangingBottom, d - scrolled)
                    oHelper.offsetChildren(-scrollBy)
                    scrolled += scrollBy
                    if (lastChildPosition == state.itemCount - 1) break
                    fillTowardsEnd(recycler)
                }
                scrolled
            }
            else -> 0
        }.also {
            recycleViewsOutOfBounds(recycler)
            updateAnchorOffset()
        }

    }

    private fun updateAnchorOffset() {
        anchorOffset =
            if (childCount > 0) {
                val view = getChildAt(0)!!
                oHelper.getDecoratedStart(view) - paddingTop
            } else 0
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

    private fun recycleViewsOutOfBounds(recycler: RecyclerView.Recycler) {
        if (childCount == 0) return
        val childCount = childCount

        var firstVisibleChild = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)!!
            val top = if (clipToPadding) paddingTop else 0
            if (oHelper.getDecoratedEnd(child) < top) {
                firstVisibleChild++
            } else {
                break
            }
        }

        var lastVisibleChild = firstVisibleChild

        for (i in lastVisibleChild until childCount) {
            val child = getChildAt(i)!!
            if (oHelper.getDecoratedStart(child) <= if (clipToPadding) height - paddingBottom else height) {
                lastVisibleChild++
            } else {
                lastVisibleChild--
                break
            }
        }

        for (i in childCount - 1 downTo lastVisibleChild + 1) removeAndRecycleViewAt(i, recycler)
        for (i in firstVisibleChild - 1 downTo 0) removeAndRecycleViewAt(i, recycler)

        anchorPosition += firstVisibleChild
    }

    private fun isRowCompleted(
        positionInRow: Int,
        isInChildRow: Boolean,
        reverse: Boolean
    ): Boolean {
        return if (reverse) {
            positionInRow == 0
        } else {
            val limit = if (isInChildRow) colCount - 1 else colCount
            // +1 to get an non zero based index
            positionInRow + 1 == limit
        }
    }

    private fun isItemInChildRow(position: Int): Boolean {
        val threshold = colCount - 1
        return position % groupItemCount > threshold
    }

    private fun getPositionInRow(position: Int, childRow: Boolean): Int {
        return if (childRow) {
            position % groupItemCount - colCount
        } else {
            position % groupItemCount
        }
    }

    private infix fun Int.apply(value: Double) = (this * value).roundToInt()

    override fun onDetachedFromWindow(view: RecyclerView?, recycler: RecyclerView.Recycler) {
        super.onDetachedFromWindow(view, recycler)
        removeAndRecycleAllViews(recycler)
        recycler.clear()
    }
}
