package com.louis.app.cavity.ui.home

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.OrientationHelper.createHorizontalHelper
import androidx.recyclerview.widget.OrientationHelper.createVerticalHelper
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.ui.home.HoneycombLayoutManager.Orientation.HORIZONTAL
import com.louis.app.cavity.ui.home.HoneycombLayoutManager.Orientation.VERTICAL
import com.louis.app.cavity.util.L
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
    RecyclerView.LayoutManager() {

    companion object {
        // 3/4 is the pointy part ratio (compared to its bounds length) of the hexagon
        private const val OVERLAPING_FACTOR = 0.25
    }

    enum class Orientation {
        VERTICAL,
        HORIZONTAL,
    }

    private val groupItemCount = (2 * colCount) - 1
    private val oHelper =
        if (orientation == VERTICAL) createVerticalHelper(this)
        else createHorizontalHelper(this)

    private var anchorPosition = 0
    private var anchorOffset = 0
    private var prefetchRange = IntRange(0, 0)
    private var extra = 0 // Predictive animations

    var delayRecycling = false

    init {
        if (colCount < 2) {
            throw IllegalArgumentException("Honeycomb layout manager require at least two rows.")
        }

        isItemPrefetchEnabled = true
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        L.v("onLayoutChildren")
        recycler.setViewCacheSize(colCount)
        detachAndScrapAttachedViews(recycler)

        if (state.itemCount > 0) {
            val extra = if (state.isPreLayout) extra else 0
            fillTowardsEnd(recycler, extra, state.isPreLayout)
        }
    }

    private fun fillTowardsEnd(
        recycler: RecyclerView.Recycler,
        extra: Int = 0,
        isPreLayout: Boolean = false
    ) {
        val toFill =
            oHelper.endAfterPadding + extra + if (clipToPadding) 0 else oHelper.endPadding
        var filled: Int // No used currently. Might be necessary to better compute actual scrolled distance in doOnScroll()
        val marginX: Int
        val marginY: Int
        val startPos: Int
        var start: Int

        if (childCount > 0) {
            val lastChild = getChildAt(childCount - 1)!!
            val lastChildPos = getPosition(lastChild)

            marginX = lastChild.marginLeft + lastChild.marginRight
            marginY = lastChild.marginTop + lastChild.marginBottom

            val towardsEndSide =
                if (orientation == VERTICAL) lastChild.measuredHeight + marginY
                else lastChild.measuredWidth + marginX

            this.extra = towardsEndSide
            L.v("extra:  $extra")
            startPos = lastChildPos + 1
            start = oHelper.getDecoratedEnd(lastChild) - (towardsEndSide apply OVERLAPING_FACTOR)
            filled = start
        } else {
            startPos = ensureStartLayoutFromRowBeiginning(anchorPosition)
            filled = 0
            start = oHelper.startAfterPadding + if (anchorPosition < itemCount) anchorOffset else 0
        }

        for (i in startPos until itemCount) {
            if (start > toFill) {
                break
            }

            val isInChildRow = isItemInChildRow(i)
            val positionInRow = getPositionInRow(i, isInChildRow)
            val isRowCompleted = isRowCompleted(positionInRow, isInChildRow, reverse = false)

//          Anonther way to fix mis layout behavirs on screen rotation from recyclerview's bottom
//          Fiexed by the ensureStartLayoutFromRowBeiginning()
//            val faultyLayout = positionInRow != 0 && startPos == anchorPosition && i == startPos
//
//            if (faultyLayout) {
//                // Depending on anchor position, the first view to be laid out (after rotation)
//                // might be laid out in the presumed postion in row (base on anchorPosition)
//                // However, other views from this row might not have been laid out
//                // (cause: hexagon size change, colCount cahnge), resulting in a blank.
//                // To fix this, we still need to layout the view to get its coordinates and update
//                // the anchor, but then we remove it to let them be laid out by fillTowardsStart()
//                // when user start scrolling to top
//                anchorPosition++
//                continue
//            }

            val view = recycler.getViewForPosition(i)
            addView(view)

            L.v("anchorPOsition: $anchorPosition")
            L.v("i: $i")

            val (towardsEndSide, otherSide) = measureOriented(view)

//            if (isPreLayout) {
//                toFill += towardsEndSide
//            }

            val left = getLeft(otherSide, positionInRow, isInChildRow)
            val end = start + towardsEndSide
            val right = left + otherSide

            layoutOriented(view, start, end, left, right)

            if (isRowCompleted) {
                start = end - (towardsEndSide apply OVERLAPING_FACTOR)
                filled += towardsEndSide apply OVERLAPING_FACTOR

                // Only prefecth items for last item in row. So the computation happens only once
                // TODO: consider giving offset for layoutRegistry.addposition here
                updatePrefetchPosition(i, isInChildRow, reverse = false)
            }
        }
    }

    private fun fillTowardsStart(recycler: RecyclerView.Recycler) {
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

        var filled = oHelper.getDecoratedStart(firstChild)

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

            L.v("anchorPOsition: $anchorPosition")

            anchorPosition--

            val (towardsEndSide, otherSide) = measureOriented(view)

            // TODO: consider moving this before if(end < toFill) to update prefecth event if we dont have to layout a new view
            val isInChildRow = isItemInChildRow(i)
            val positionInRow = getPositionInRow(i, isInChildRow)
            val isRowCompleted = isRowCompleted(positionInRow, isInChildRow, reverse = true)

            val left = getLeft(otherSide, positionInRow, isInChildRow)
            val start = end - towardsEndSide
            val right = left + otherSide

            layoutOriented(view, start, end, left, right)

            if (isRowCompleted) {
                end = start + (towardsEndSide apply OVERLAPING_FACTOR)
                filled += towardsEndSide

                // Only prefecth items for last item in row. So the computation happens only once
                // TODO: consider giving offset for layoutRegistry.addposition here
                updatePrefetchPosition(i, isInChildRow, reverse = true)
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
                oHelper.getDecoratedStart(view) - oHelper.startAfterPadding
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
     * This method returns the row start position based in the row anchorPosition is and updates it
     */
    private fun ensureStartLayoutFromRowBeiginning(position: Int): Int {
        if (anchorPosition >= itemCount) {
            return 0
        }

        return getFirstItemInRowPositionFromAnchorPosisiton(position).also {
            anchorPosition = it
        }
    }

    private fun getMainAxisPadding() = oHelper.startAfterPadding + oHelper.endPadding

    private infix fun Int.apply(value: Double) = (this * value).roundToInt()

    override fun onDetachedFromWindow(view: RecyclerView?, recycler: RecyclerView.Recycler) {
        super.onDetachedFromWindow(view, recycler)

        val recycling = {
            removeAndRecycleAllViews(recycler)
            recycler.clear()
        }

        L.v("recycling")

        if (delayRecycling) {
            recycling()
//            view?.postDelayed(500, recycling)
        } else {
            recycling()
        }
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
            L.v("prefecth registry updated from : $start to $end")
        } else {
            start = currentLayoutPos - colCount + childRowFactor
            end = currentLayoutPos - 1
            L.v("prefecth registry updated from : $start to $end")
        }

        prefetchRange = start..end
    }

    override fun collectAdjacentPrefetchPositions(
        dx: Int,
        dy: Int,
        state: RecyclerView.State,
        layoutPrefetchRegistry: LayoutPrefetchRegistry
    ) {
        L.v("collectAdjacent")
        val delta = if (orientation === HORIZONTAL) dx else dy
        val offset = abs(delta)
        val invalidScroll = childCount == 0 || delta == 0

        if (invalidScroll) {
            return
        }

        prefetchRange.forEach {
            // TODO: consider using coerceIn() in updatePrefetch positions
            if (it > 0 && it < state.itemCount) {
                layoutPrefetchRegistry.addPosition(it, offset)
            }
        }
    }

    override fun collectInitialPrefetchPositions(
        adapterItemCount: Int,
        layoutPrefetchRegistry: LayoutPrefetchRegistry?
    ) {
        L.v("collectInitialPrefetchPositions")
        super.collectInitialPrefetchPositions(adapterItemCount, layoutPrefetchRegistry)
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

    override fun onSaveInstanceState(): Parcelable = HoneycombState(anchorPosition, anchorOffset)

    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? HoneycombState)?.let {
            anchorPosition = state.anchorPosition
            anchorOffset = state.anchorOffset
        }
    }
}
