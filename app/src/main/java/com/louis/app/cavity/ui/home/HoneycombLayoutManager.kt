package com.louis.app.cavity.ui.home

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.recyclerview.widget.OrientationHelper.createHorizontalHelper
import androidx.recyclerview.widget.OrientationHelper.createVerticalHelper
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.ui.home.HoneycombLayoutManager.Orientation.HORIZONTAL
import com.louis.app.cavity.ui.home.HoneycombLayoutManager.Orientation.VERTICAL
import com.louis.app.cavity.util.L
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
    private var extra = 0 // Predictive animations

    init {
        if (colCount < 2) {
            throw IllegalArgumentException("Honeycomb layout manager require at least two rows.")
        }
    }

    // TODO: fix weird anchor position when scrolling to end and turn phone in horizontal mode

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        detachAndScrapAttachedViews(recycler)

        if (state.itemCount > 0) {
            val extra = if (state.isPreLayout) extra else 0
            fillTowardsEnd(recycler, extra)
        }
    }

    private fun fillTowardsEnd(recycler: RecyclerView.Recycler, extra: Int = 0) {
        val toFill = if (clipToPadding) oHelper.endAfterPadding + extra else oHelper.endAfterPadding + extra
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
            startPos = lastChildPos + 1
            start = oHelper.getDecoratedEnd(lastChild) - (towardsEndSide apply OVERLAPING_FACTOR)
            filled = start
        } else {
            startPos = if (anchorPosition < itemCount) anchorPosition else 0
            filled = 0
            start = oHelper.startAfterPadding + if (anchorPosition < itemCount) anchorOffset else 0
        }

        for (i in startPos until itemCount) {
            if (start > toFill) {
                break
            }

            val towardsEndSide: Int
            val otherSide: Int
            val view = recycler.getViewForPosition(i)

            addView(view)

            if (orientation == VERTICAL) {
                measureChildWithMargins(view, width - (width / colCount), 0)
                towardsEndSide = view.measuredHeight + view.marginTop + view.marginBottom
                otherSide = view.measuredWidth + view.marginLeft + view.marginRight
            } else {
                measureChildWithMargins(view, 0, height - (height / colCount))
                towardsEndSide = view.measuredWidth + view.marginLeft + view.marginRight
                otherSide = view.measuredHeight + view.marginTop + view.marginBottom
            }

            val isInChildRow = isItemInChildRow(i)
            val positionInRow = getPositionInRow(i, isInChildRow)
            val isRowCompleted = isRowCompleted(positionInRow, isInChildRow, reverse = false)

            val left = if (isInChildRow) {
                val childRowOffset = otherSide / 2
                childRowOffset + otherSide * positionInRow
            } else {
                otherSide * positionInRow
            }

            val end = start + towardsEndSide
            val right = left + otherSide

            layoutOriented(view, start, end, left, right)

            if (isRowCompleted) {
                start = end - (towardsEndSide apply OVERLAPING_FACTOR)
                filled += towardsEndSide apply OVERLAPING_FACTOR
            }
        }
    }

    private fun fillTowardsStart(recycler: RecyclerView.Recycler) {
        val marginX: Int
        val marginY: Int
        var end: Int


        if (childCount == 0) return

        val firstChild = getChildAt(0)!!
        val firstChildPos = getPosition(firstChild)



        if (firstChildPos == 0) return

        var filled = oHelper.getDecoratedStart(firstChild)

        marginX = firstChild.marginLeft + firstChild.marginRight
        marginY = firstChild.marginTop + firstChild.marginBottom

        val toFill = if (clipToPadding) oHelper.startAfterPadding else 0

        val towardsEndLastSide =
            if (orientation == VERTICAL) firstChild.measuredHeight + marginY
            else firstChild.measuredWidth + marginX

        end = oHelper.getDecoratedStart(firstChild) + (towardsEndLastSide apply OVERLAPING_FACTOR)

        for (i in firstChildPos - 1 downTo 0) {
            if (end < toFill) break

            val towardsEndSide: Int
            val otherSide: Int
            val view = recycler.getViewForPosition(i)

            addView(view, 0)

            anchorPosition--

            if (orientation == VERTICAL) {
                measureChildWithMargins(view, width - (width / colCount), 0)
                towardsEndSide = view.measuredHeight + view.marginTop + view.marginBottom
                otherSide = view.measuredWidth + view.marginLeft + view.marginRight
            } else {
                measureChildWithMargins(view, 0, height - (height / colCount))
                towardsEndSide = view.measuredWidth + view.marginLeft + view.marginRight
                otherSide = view.measuredHeight + view.marginTop + view.marginBottom
            }

            val isInChildRow = isItemInChildRow(i)
            val positionInRow = getPositionInRow(i, isInChildRow)
            val isRowCompleted = isRowCompleted(positionInRow, isInChildRow, reverse = true)

            val left = if (isInChildRow) {
                val childRowOffset = otherSide / 2
                childRowOffset + otherSide * positionInRow
            } else {
                otherSide * positionInRow
            }

            val start = end - towardsEndSide
            val right = left + otherSide

            layoutOriented(view, start, end, left, right)

            if (isRowCompleted) {
                end = start + (towardsEndSide apply OVERLAPING_FACTOR)
                filled += towardsEndSide
            }
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
                val clipPadding = when {
                    !clipToPadding -> if (orientation == VERTICAL) paddingTop else paddingLeft
                    else -> 0
                }
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
                val padding = if (orientation == VERTICAL) paddingTop else paddingLeft
                oHelper.getDecoratedStart(view) - padding
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
        // Le souvcis initial était que les vues qui devaient etre layout vers le bas au fur et à mesure que l'on scrolle étaient recyclée tout de suite
        // Pour utiliser un clippading false avec du adding top, on peut ajouter le padding top à la limite de recyclage.
        // Mainteneant il faudrait faire vérifier que ça marche pour le padding bottom, et le faire fonctionner pour quand ya les deux paddings en même temps
        if (childCount == 0) {
            return
        }

        val childCount = childCount
        val clipPadding = when {
            !clipToPadding -> if (orientation == VERTICAL) paddingTop else paddingLeft
            else -> 0
        }

        var firstVisibleChild = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)!!
            val padding = if (orientation == VERTICAL) paddingTop else paddingLeft
            val top = if (clipToPadding) padding else 0
            if (oHelper.getDecoratedEnd(child) < top) {
                firstVisibleChild++
            } else {
                break
            }
        }

        var lastVisibleChild = firstVisibleChild

        for (i in lastVisibleChild until childCount) {
            val child = getChildAt(i)!!
            val padding = if (orientation == VERTICAL) paddingBottom else paddingRight
            val limit = if (clipToPadding) oHelper.totalSpace - padding else oHelper.totalSpace + paddingTop

            if (oHelper.getDecoratedStart(child) <= limit) {
                lastVisibleChild++
            } else {
                lastVisibleChild--
                break
            }
        }

        for (i in childCount - 1 downTo lastVisibleChild + 1) {
            L.v("recycle view at bottom")
            removeAndRecycleViewAt(i, recycler)
        }

        for (i in firstVisibleChild - 1 downTo 0) {
            removeAndRecycleViewAt(i, recycler)
            L.v("recycle view at top")
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

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
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
