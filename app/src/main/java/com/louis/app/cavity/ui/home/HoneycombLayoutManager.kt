package com.louis.app.cavity.ui.home

import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.OrientationHelper.createHorizontalHelper
import androidx.recyclerview.widget.OrientationHelper.createVerticalHelper
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.ui.bottle.adapter.JumpSmoothScroller
import com.louis.app.cavity.ui.home.HoneycombLayoutManager.Orientation.HORIZONTAL
import com.louis.app.cavity.ui.home.HoneycombLayoutManager.Orientation.VERTICAL
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Organize the views in a honeycomb fashion
 * Even rows will contain rowCount items, odd rows will contain rowCount - 1 items
 * Thus rowCount must be at least 2
 *
 * Thought to be used with HexagonalView
 *
 * To get nice patterns, set the HexagonalView's flat attribute to true when using HORIZONTAL
 * orientation otherwise false
 */
class HoneycombLayoutManager(private val colCount: Int, private val orientation: Orientation) :
    RecyclerView.LayoutManager(), RecyclerView.SmoothScroller.ScrollVectorProvider {

    companion object {
        // 3/4 is the pointy part ratio (compared to its bounds length) of the hexagon
        private const val OVERLAPING_FACTOR = 0.25
    }

    enum class Orientation {
        VERTICAL,
        HORIZONTAL,
    }

    inner class LayoutState(
        var anchorPosition: Int,
        var anchorOffset: Int,
        var extraLayoutRange: Int, // Predictive animations
        var prefetchRange: IntRange = 0..0
    ) {
        fun updateAnchorOffset() {
            anchorOffset =
                if (childCount > 0) {
                    val view = getChildAt(0)!!
                    oHelper.getDecoratedStart(view) - oHelper.startAfterPadding
                } else 0
        }

        fun computeAnchorOffset(): Int {
            return if (anchorPosition < itemCount) anchorOffset else 0
        }
    }

    data class Configuration(
        /**
         * Set this to true to enable view recycling as soon as the associated recycler view is
         * detached from window. This will make views available to recycler view pool instantly
         * for other recycler views to use.
         */
        var recycleOnDetach: Boolean,

        /**
         * It seems like onDetachedFromWindow is called once when starting to transition out of fragment
         * and also after fragment transaction completes.
         * If this parameter is set to true, the view won't be recycled right away, leaving room for
         * fragment transiton to occur with all recycler view items. Then they'll be recycled
         */
        var skipNextRecycleOnDetach: Boolean,

        /**
         * If requiring a smooth scroll, jump to item - jumpScrollThreshold position before smooth
         * scrolling to prevent long scroll it item is far. If <= 0, it will not jump scroll
         */
        var jumpScrollThreshold: Int
    )

    private val layoutState = LayoutState(0, 0, 0, 0..0)
    private val groupItemCount = (2 * colCount) - 1
    private val oHelper =
        if (orientation == VERTICAL) createVerticalHelper(this)
        else createHorizontalHelper(this)

    val config = Configuration(
        recycleOnDetach = true,
        skipNextRecycleOnDetach = false,
        jumpScrollThreshold = 0
    )

    init {
        if (colCount < 2) {
            throw IllegalArgumentException("Honeycomb layout manager require at least two rows.")
        }

        isItemPrefetchEnabled = true
    }

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        recycler.setViewCacheSize(colCount)
        super.onMeasure(recycler, state, widthSpec, heightSpec)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        detachAndScrapAttachedViews(recycler)

        if (state.itemCount > 0) {
            fillTowardsEnd(recycler, state)
        }
    }

    private fun fillTowardsEnd(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) {
        val toFill = oHelper.endAfterPadding + if (clipToPadding) 0 else oHelper.endPadding
        val marginX: Int
        val marginY: Int
        val startPos: Int
        var start: Int

        if (childCount > 0) {
            val lastChild = getLastChild()!!
            val lastChildPos = getPosition(lastChild)

            marginX = lastChild.marginLeft + lastChild.marginRight
            marginY = lastChild.marginTop + lastChild.marginBottom

            val towardsEndSide =
                if (orientation == VERTICAL) lastChild.measuredHeight + marginY
                else lastChild.measuredWidth + marginX

            startPos = lastChildPos + 1
            start = oHelper.getDecoratedEnd(lastChild) - (towardsEndSide apply OVERLAPING_FACTOR)
        } else {
            startPos = ensureStartLayoutFromRowBeiginning()
            start = oHelper.startAfterPadding + layoutState.computeAnchorOffset()
        }

        for (i in startPos until state.itemCount) {
            if (start > toFill + layoutState.extraLayoutRange) {
                break
            }

            val isInChildRow = isItemInChildRow(i)
            val positionInRow = getPositionInRow(i, isInChildRow)
            val isRowCompleted = isRowCompleted(positionInRow, isInChildRow, reverse = false)

            val view = recycler.getViewForPosition(i)
            addView(view)

            val (towardsEndSide, otherSide) = measureOriented(view)

            val left = getLeft(otherSide, positionInRow, isInChildRow)
            val end = start + towardsEndSide
            val right = left + otherSide

            layoutOriented(view, start, end, left, right)

            if (isRowCompleted) {
                start = end - (towardsEndSide apply OVERLAPING_FACTOR)

                // Only prefecth items for last item in row. So the computation happens only once
                updatePrefetchPosition(i, isInChildRow, state, reverse = false)
            }

            if (state.isPreLayout && start > toFill) {
                layoutState.extraLayoutRange = towardsEndSide apply (1 - OVERLAPING_FACTOR)
            }
        }
    }

    private fun fillTowardsStart(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        val marginX: Int
        val marginY: Int
        var end: Int

        if (childCount == 0) {
            return
        }

        val firstChild = getChildAt(0)!!
        val firstChildPos = getPosition(firstChild)

        if (firstChildPos == 0) {
            return
        }

        marginX = firstChild.marginLeft + firstChild.marginRight
        marginY = firstChild.marginTop + firstChild.marginBottom

        val toFill = if (clipToPadding) oHelper.startAfterPadding else 0

        val towardsEndLastSide =
            if (orientation == VERTICAL) firstChild.measuredHeight + marginY
            else firstChild.measuredWidth + marginX

        end = oHelper.getDecoratedStart(firstChild) + (towardsEndLastSide apply OVERLAPING_FACTOR)

        for (i in firstChildPos - 1 downTo 0) {
            if (end < toFill) {
                break
            }

            val view = recycler.getViewForPosition(i)
            addView(view, 0)

            layoutState.anchorPosition--

            val (towardsEndSide, otherSide) = measureOriented(view)

            val isInChildRow = isItemInChildRow(i)
            val positionInRow = getPositionInRow(i, isInChildRow)
            val isRowCompleted = isRowCompleted(positionInRow, isInChildRow, reverse = true)

            val left = getLeft(otherSide, positionInRow, isInChildRow)
            val start = end - towardsEndSide
            val right = left + otherSide

            layoutOriented(view, start, end, left, right)

            if (isRowCompleted) {
                end = start + (towardsEndSide apply OVERLAPING_FACTOR)

                // Only prefecth items for last item in row. So the computation happens only once
                updatePrefetchPosition(i, isInChildRow, state, reverse = true)
            }
        }
    }

    private fun measureOriented(view: View): Pair<Int, Int> {
        return if (orientation == VERTICAL) {
            measureChildWithMargins(view, width - (width / colCount), 0)
            val towardsEndSide = view.measuredHeight + view.marginTop + view.marginBottom
            val otherSide = view.measuredWidth + view.marginLeft + view.marginRight
            towardsEndSide to otherSide
        } else {
            measureChildWithMargins(view, 0, height - (height / colCount))
            val towardsEndSide = view.measuredWidth + view.marginLeft + view.marginRight
            val otherSide = view.measuredHeight + view.marginTop + view.marginBottom
            towardsEndSide to otherSide
        }
    }

    private fun getLeft(otherSide: Int, positionInRow: Int, isInChildRow: Boolean): Int {
        return if (isInChildRow) {
            val childRowOffset = otherSide / 2
            childRowOffset + otherSide * positionInRow
        } else {
            otherSide * positionInRow
        }
    }

    private fun layoutOriented(view: View, start: Int, end: Int, left: Int, right: Int) {
        if (orientation == VERTICAL) {
            layoutDecoratedWithMargins(view, left, start, right, end)
        } else {
            layoutDecoratedWithMargins(view, start, left, end, right)
        }
    }

    private fun doOnScroll(
        d: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        return when {
            childCount == 0 -> 0
            d < 0 -> {
                val clipPadding = if (clipToPadding) 0 else oHelper.startAfterPadding
                val toFill = if (clipToPadding) oHelper.startAfterPadding else 0
                var scrolled = 0

                while (scrolled > d) {
                    val firstChild = getChildAt(0)!!
                    val firstChildTop = oHelper.getDecoratedStart(firstChild)
                    val hangingTop = max(0, toFill - firstChildTop + clipPadding)
                    val scrollBy = min(hangingTop, scrolled - d)
                    oHelper.offsetChildren(scrollBy)
                    scrolled -= scrollBy
                    if (layoutState.anchorPosition == 0) break
                    fillTowardsStart(recycler, state)
                }
                scrolled
            }

            d > 0 -> {
                val toFill = oHelper.endAfterPadding
                var scrolled = 0

                while (scrolled < d) {
                    val lastChild = getLastChild()!!
                    val lastChildPosition = getPosition(lastChild)
                    val lastChildBottom = oHelper.getDecoratedEnd(lastChild)
                    val hangingBottom = max(0, lastChildBottom - toFill)
                    val scrollBy = min(hangingBottom, d - scrolled)
                    oHelper.offsetChildren(-scrollBy)
                    scrolled += scrollBy
                    if (lastChildPosition == state.itemCount - 1) break
                    fillTowardsEnd(recycler, state)
                }
                scrolled
            }

            else -> 0
        }.also {
            recycleViewsOutOfBounds(recycler)
            layoutState.updateAnchorOffset()
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

    private fun recycleViewsOutOfBounds(recycler: RecyclerView.Recycler) {
        if (childCount == 0) {
            return
        }

        val childCount = childCount
        var firstVisibleChild = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)!!
            val top = if (clipToPadding) oHelper.startAfterPadding else 0
            if (oHelper.getDecoratedEnd(child) < top) {
                firstVisibleChild++
            } else {
                break
            }
        }

        var lastVisibleChild = firstVisibleChild

        for (i in lastVisibleChild until childCount) {
            val child = getChildAt(i)!!
            val padding = getMainAxisPadding()
            val limit =
                if (clipToPadding) oHelper.totalSpace + oHelper.startAfterPadding
                else oHelper.totalSpace + padding

            if (oHelper.getDecoratedStart(child) <= limit) {
                lastVisibleChild++
            } else {
                lastVisibleChild--
                break
            }
        }

        for (i in childCount - 1 downTo lastVisibleChild + 1) {
            removeAndRecycleViewAt(i, recycler)
        }

        for (i in firstVisibleChild - 1 downTo 0) {
            removeAndRecycleViewAt(i, recycler)
        }

        layoutState.anchorPosition += firstVisibleChild
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
        val childRowFactor = if (childRow) colCount else 0
        return position % groupItemCount - childRowFactor
    }

    private fun getFirstItemInRowPositionFromAnchorPosisiton(position: Int): Int {
        val isInChildRow = isItemInChildRow(position)
        val childRowFactor = if (isInChildRow) colCount else 0
        val groupPosition = position % groupItemCount
        return position - groupPosition + childRowFactor
    }

    /**
     * Prevents layout glitches when fillTowardsEnd() try to pickup anchor position after screen
     * rotation. It may try to layout from anywhere in the row, depending on
     * anchorPosition & colCount values, leaving blanks in the layout.
     * This method returns the row start position based on the row anchorPosition is and updates it
     */
    private fun ensureStartLayoutFromRowBeiginning(): Int {
        if (layoutState.anchorPosition >= itemCount) {
            return 0
        }

        return getFirstItemInRowPositionFromAnchorPosisiton(layoutState.anchorPosition).also {
            layoutState.anchorPosition = it
        }
    }

    private fun getMainAxisPadding() = oHelper.startAfterPadding + oHelper.endPadding

    private infix fun Int.apply(value: Double) = (this * value).roundToInt()

    private fun getLastChild() = getChildAt(childCount - 1)

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int
    ) {
        if (config.jumpScrollThreshold > 0) {
            JumpSmoothScroller(recyclerView.context, config.jumpScrollThreshold)
        } else {
            LinearSmoothScroller(recyclerView.context)
        }
            .run {
                targetPosition = position
                startSmoothScroll(this)
            }

    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        if (childCount == 0) {
            return null
        }

        val firstChildPosition = getPosition(getChildAt(0)!!)
        val direction = if (targetPosition < firstChildPosition) -1 else 1

        return if (orientation == HORIZONTAL) {
            PointF(direction.toFloat(), 0f)
        } else {
            PointF(0f, direction.toFloat())
        }
    }

    override fun onDetachedFromWindow(view: RecyclerView?, recycler: RecyclerView.Recycler) {
        super.onDetachedFromWindow(view, recycler)

        if (config.skipNextRecycleOnDetach || !config.recycleOnDetach) {
            config.skipNextRecycleOnDetach = false
            return
        }

        removeAndRecycleAllViews(recycler)
        recycler.clear()
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    private fun updatePrefetchPosition(
        currentLayoutPos: Int,
        isInChildRow: Boolean,
        state: RecyclerView.State,
        reverse: Boolean
    ) {
        val childRowFactor = when {
            isInChildRow -> 0
            reverse -> 1
            else -> -1
        }

        val start: Int
        val end: Int

        if (!reverse) {
            start = currentLayoutPos + 1
            end = currentLayoutPos + colCount + childRowFactor
        } else {
            start = currentLayoutPos - colCount + childRowFactor
            end = currentLayoutPos - 1
        }

        layoutState.prefetchRange =
            start.coerceIn(0, state.itemCount - 1)..end.coerceIn(0, state.itemCount - 1)
    }

    override fun collectAdjacentPrefetchPositions(
        dx: Int,
        dy: Int,
        state: RecyclerView.State,
        layoutPrefetchRegistry: LayoutPrefetchRegistry
    ) {
        val delta = if (orientation === HORIZONTAL) dx else dy
        val offset = abs(delta)
        val invalidScroll = childCount == 0 || delta == 0

        if (invalidScroll) {
            return
        }

        layoutState.prefetchRange.forEach {
            layoutPrefetchRegistry.addPosition(it, offset)
        }
    }

    override fun supportsPredictiveItemAnimations() = true

    data class HoneycombState(val anchorPosition: Int, val anchorOffset: Int) : Parcelable {
        constructor(parcel: Parcel) : this(parcel.readInt(), parcel.readInt())

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(anchorPosition)
            dest.writeInt(anchorOffset)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<HoneycombState> {
            override fun createFromParcel(parcel: Parcel): HoneycombState = HoneycombState(parcel)
            override fun newArray(size: Int): Array<HoneycombState?> = arrayOfNulls(size)
        }
    }

    override fun onSaveInstanceState(): Parcelable =
        HoneycombState(layoutState.anchorPosition, layoutState.anchorOffset)

    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? HoneycombState)?.let {
            layoutState.anchorPosition = state.anchorPosition
            layoutState.anchorOffset = state.anchorOffset
        }
    }
}
