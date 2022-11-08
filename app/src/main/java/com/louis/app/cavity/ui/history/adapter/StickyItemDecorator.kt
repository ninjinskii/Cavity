package com.louis.app.cavity.ui.history.adapter

import android.graphics.Canvas
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.withTranslation
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.util.doOnEachNextLayout

class StickyItemDecorator(
    parent: RecyclerView,
    private val isHeader: (itemPosition: Int) -> Boolean,
    private val onHeaderClick: () -> Unit
) :
    RecyclerView.ItemDecoration(), GestureDetector.OnGestureListener {

    private val detector = GestureDetectorCompat(parent.context, this)
    private var currentHeader: Pair<Int, RecyclerView.ViewHolder>? = null

    init {
        parent.adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                // clear saved header as it can be outdated now
                currentHeader = null
            }
        })

        parent.doOnEachNextLayout {
            // clear saved layout as it may need layout update
            currentHeader = null
        }

        parent.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(
                recyclerView: RecyclerView,
                event: MotionEvent
            ): Boolean {
                val stickyHeaderBottom = currentHeader?.second?.itemView?.bottom ?: 0

                return event.run {
                    action != MotionEvent.ACTION_MOVE &&
                        action != MotionEvent.ACTION_UP &&
                        y <= stickyHeaderBottom
                }
            }

            override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {
                detector.onTouchEvent(event)
            }
        })
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        onHeaderClick()
        return true
    }

    override fun onDown(e: MotionEvent) = true

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val topChild = parent.findChildViewUnder(
            parent.paddingLeft.toFloat(),
            parent.paddingTop.toFloat()
        ) ?: return
        val topChildPosition = parent.getChildAdapterPosition(topChild)

        if (topChildPosition == RecyclerView.NO_POSITION) {
            return
        }

        val headerView = getHeaderViewForItem(topChildPosition, parent) ?: return

        val contactPoint = headerView.bottom + parent.paddingTop
        val childInContact = getChildInContact(parent, contactPoint) ?: return
        val childInContactPos = parent.getChildAdapterPosition(childInContact)

        if (childInContactPos >= 0 && isHeader(childInContactPos)) {
            moveHeader(c, headerView, childInContact, parent.paddingTop)
            return
        }

        drawHeader(c, headerView, parent.paddingTop)
    }

    private fun getHeaderViewForItem(itemPosition: Int, parent: RecyclerView): View? {
        if (parent.adapter == null) {
            return null
        }

        val headerPosition = getHeaderPositionForItem(itemPosition)
        if (headerPosition == RecyclerView.NO_POSITION) return null
        val headerType = parent.adapter?.getItemViewType(headerPosition) ?: return null

        if (currentHeader?.first == headerPosition && currentHeader?.second?.itemViewType == headerType) {
            return currentHeader?.second?.itemView
        }

        val headerHolder = parent.adapter?.createViewHolder(parent, headerType)
        if (headerHolder != null) {
            parent.adapter?.onBindViewHolder(headerHolder, headerPosition)
            fixLayoutSize(parent, headerHolder.itemView)

            currentHeader = headerPosition to headerHolder
        }

        return headerHolder?.itemView
    }

    private fun drawHeader(c: Canvas, header: View, paddingTop: Int) {
        c.withTranslation(0f, paddingTop.toFloat()) {
            header.draw(this)
        }
    }

    private fun moveHeader(c: Canvas, currentHeader: View, nextHeader: View, paddingTop: Int) {
        c.clipRect(0, paddingTop, c.width, paddingTop + currentHeader.height)
        c.withTranslation(0f, (nextHeader.top - currentHeader.height).toFloat()) {
            currentHeader.draw(this)
        }
    }

    private fun getChildInContact(parent: RecyclerView, contactPoint: Int): View? {
        var childInContact: View? = null
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val mBounds = Rect()
            parent.getDecoratedBoundsWithMargins(child, mBounds)
            if (mBounds.bottom > contactPoint) {
                if (mBounds.top <= contactPoint) {
                    // This child overlaps the contactPoint
                    childInContact = child
                    break
                }
            }
        }
        return childInContact
    }

    private fun fixLayoutSize(parent: ViewGroup, view: View) {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec =
            View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

        val childWidthSpec = ViewGroup.getChildMeasureSpec(
            widthSpec,
            parent.paddingLeft + parent.paddingRight,
            view.layoutParams.width
        )
        val childHeightSpec = ViewGroup.getChildMeasureSpec(
            heightSpec,
            parent.paddingTop + parent.paddingBottom,
            view.layoutParams.height
        )

        view.measure(childWidthSpec, childHeightSpec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    private fun getHeaderPositionForItem(itemPosition: Int): Int {
        var headerPosition = RecyclerView.NO_POSITION
        var currentPosition = itemPosition
        do {
            if (isHeader(currentPosition)) {
                headerPosition = currentPosition
                break
            }
            currentPosition -= 1
        } while (currentPosition >= 0)

        return headerPosition
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, dX: Float, dY: Float) = false

    override fun onLongPress(e: MotionEvent) {
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, vX: Float, vY: Float) = false
}
