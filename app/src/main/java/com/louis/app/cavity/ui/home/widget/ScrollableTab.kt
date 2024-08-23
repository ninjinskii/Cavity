package com.louis.app.cavity.ui.home.widget

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.core.content.res.use
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.shape.MaterialShapeDrawable
import com.louis.app.cavity.R
import kotlin.math.pow

class ScrollableTab @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    RecyclerView(context, attrs, defStyleAttr) {

    private val snapHelper: LinearSnapHelper
    private val layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
    private var viewPager: ViewPager2? = null
    private var isRVScrolling = true
    private var pagerInitiatedScroll = false
    private var pageChangeListener: ((position: Int) -> Unit)? = null
    private var tabChangeListener: ((position: Int) -> Unit)? = null
    private var selectedColor = Color.WHITE
    private var unSelectedColor = Color.GRAY
    private var position = 0

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ScrollableTab).use {
            background = MaterialShapeDrawable.createWithElevationOverlay(context, elevation)
            selectedColor = it.getColor(R.styleable.ScrollableTab_selectedColor, Color.WHITE)
            unSelectedColor =
                it.getColor(R.styleable.ScrollableTab_unSelectedColor, Color.GRAY)
        }

        setLayoutManager(layoutManager)
        setHasFixedSize(true)

        snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(this)

        createPagerStyle()
        swallowTouchEvents()

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)

                children.forEach {
                    val childCenterX = (it.left + it.right) / 2
                    val scaleValue =
                        getGaussianScale(childCenterX, 1f, 1f, 150.toDouble(), left, right)
                    colorView(it, scaleValue)
                }
            }

            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == SCROLL_STATE_IDLE) {
                    val child = snapHelper.findSnapView(layoutManager) ?: return
                    if (isRVScrolling) {
                        val position = layoutManager.getPosition(child)
                        viewPager?.setCurrentItem(position, true)

                        if (this@ScrollableTab.position != position) {
                            this@ScrollableTab.position = position
                            tabChangeListener?.invoke(position)
                        }
                    }
                }
            }
        })
    }

    override fun setElevation(elevation: Float) {
        super.setElevation(elevation)
        val bg = background
        if (bg is MaterialShapeDrawable) bg.z = z
    }

    private fun createPagerStyle() {
        clipToPadding = false
        val halfWidth = context.resources.displayMetrics.widthPixels / 2
        val padding = halfWidth / 2
        setPadding(padding, 0, padding, 0)
    }

    // We want to use touch listener as an indicator that the recyclerview might be scrolled
    @SuppressLint("ClickableViewAccessibility")
    private fun swallowTouchEvents() {
        setOnTouchListener { _, _ ->
            isRVScrolling = true
            false
        }
    }

    fun addOnTabChangeListener(tabChangeListener: ((position: Int) -> Unit)) {
        this.tabChangeListener = tabChangeListener
    }

    fun addOnPageChangeListener(pageChangeListener: (position: Int) -> Unit) {
        this.pageChangeListener = pageChangeListener
    }

    fun setUpWithViewPager(viewPager: ViewPager2) {
        if (viewPager.adapter == null) {
            return
        }

        this.viewPager = viewPager

        var lengthRatio = 1f

        // Some adjustments need to be done if ViewPage scrolling distance is not the same as ScrollableTab width
        if (viewPager.orientation == ViewPager2.ORIENTATION_VERTICAL) {
            val viewPagerHeight = viewPager.measuredHeight.toFloat()
            val tabWidth = measuredWidth
            lengthRatio = tabWidth / viewPagerHeight
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    isRVScrolling = false
                    pagerInitiatedScroll = true
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (!isRVScrolling) {
                    layoutManager.scrollToPositionWithOffset(
                        position,
                        ((-positionOffsetPixels * lengthRatio) / 2).toInt()
                    )
                }
            }

            override fun onPageSelected(position: Int) {
                isRVScrolling = !pagerInitiatedScroll
                pagerInitiatedScroll = false
                smoothScrollToPosition(position)
                pageChangeListener?.invoke(position)
            }
        })
    }

    fun moveToView(view: View) {
        val d = snapHelper.calculateDistanceToFinalSnap(layoutManager, view)

        if (d != null && d[0] != 0) {
            smoothScrollBy(d[0], 0)
        }
    }

    private fun colorView(child: View, scaleValue: Float) {
        val percent = (scaleValue - 1) / 1f
        val color = ArgbEvaluator().evaluate(percent, unSelectedColor, selectedColor) as Int
        child.findViewById<TextView>(R.id.county)
            .setTextColor(color)
    }

    fun getGaussianScale(
        childCenterX: Int,
        minScaleOffset: Float,
        scaleFactor: Float,
        spreadFactor: Double,
        left: Int,
        right: Int
    ): Float {
        val recyclerCenterX = (left + right) / 2
        return (Math.E.pow(
            -(childCenterX - recyclerCenterX.toDouble()).pow(2.toDouble()) / (2 * spreadFactor.pow(
                2.toDouble()
            ))
        ) * scaleFactor + minScaleOffset).toFloat()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        this.pageChangeListener = null
        viewPager = null
    }
}
