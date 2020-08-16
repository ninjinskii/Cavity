package com.louis.app.cavity.ui

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.louis.app.cavity.R
import kotlin.math.pow

class CountyScrollableTab : RecyclerView {

    private val tabAdapter by lazy { TabAdapter(style = tabTextStyle) }
    private var selectedColor = Color.WHITE
    private var unSelectedColor = Color.GRAY
    private var tabTextStyle = TabStyle(R.style.TabTextAppearance)
    private val layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
    private var viewPager: ViewPager? = null
    private var isRVScrolling = true
    private var listener: ((position: Int) -> Unit)? = null
    private var pageChangeListener: ((position: Int) -> Unit)? = null

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        init(attrs)
    }

    private fun init(set: AttributeSet?) {
        initAttributes(set)

        setLayoutManager(layoutManager)
        setHasFixedSize(true)
        adapter = tabAdapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(this)

        createPagerStyle()

        setOnTouchListener { _, _ ->
            isRVScrolling = true
            performClick()
            false
        }
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)
                post {
                    (0 until childCount).forEach {
                        val child = getChildAt(it)
                        val childCenterX = (child.left + child.right) / 2
                        val scaleValue =
                            getGaussianScale(childCenterX, 1f, 1f, 150.toDouble(), left, right)
                        colorView(child, scaleValue)
                    }
                }
            }

            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == SCROLL_STATE_IDLE) {
                    val child = snapHelper.findSnapView(layoutManager) ?: return
                    if (isRVScrolling) viewPager?.setCurrentItem(
                        layoutManager.getPosition(child),
                        true
                    )
                }
            }
        })

        tabAdapter.onTabClick {
            isRVScrolling = true
            listener?.invoke(it)
            smoothScrollToPosition(it)
            viewPager?.setCurrentItem(it, true)
        }
    }

    private fun initAttributes(set: AttributeSet?) {
        val ta = context.obtainStyledAttributes(set, R.styleable.CountyScrollableTab)
        selectedColor = ta.getColor(R.styleable.CountyScrollableTab_selectedColor, Color.WHITE)
        unSelectedColor = ta.getColor(R.styleable.CountyScrollableTab_unSelectedColor, Color.GRAY)
        tabTextStyle =
            TabStyle(
                ta.getResourceId(
                    R.styleable.CountyScrollableTab_tabTextAppearance,
                    R.style.TabTextAppearance
                )
            )
        ta.recycle()

    }

    private fun createPagerStyle() {
        clipToPadding = false
        val halfSWidth = context.resources.displayMetrics.widthPixels / 2
        val padding = halfSWidth / 2
        setPadding(padding, 0, padding, 0)
    }

    fun addTabs(list: List<String>) {
        tabAdapter.addAll(list)
    }

    fun addOnTabListener(listener: (position: Int) -> Unit) {
        this.listener = listener
    }

    fun addOnPageChangeListener(pageChangeListener: (position: Int) -> Unit) {
        this.pageChangeListener = pageChangeListener
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setUpWithViewPager(viewPager: ViewPager) {
        if (viewPager.adapter == null) throw IllegalStateException(
            "ViewPager does not have pager adapter"
        )
        this.viewPager = viewPager

        viewPager.setOnTouchListener { _, _ ->
            isRVScrolling = false
            false
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (!isRVScrolling)
                    layoutManager.scrollToPositionWithOffset(position, -positionOffsetPixels / 2)
            }

            override fun onPageSelected(position: Int) {
                smoothScrollToPosition(position)
                pageChangeListener?.invoke(position)
            }
        })
    }

    private fun colorView(
        child: View,
        scaleValue: Float
    ) {
        val percent = (scaleValue - 1) / 1f
        val color = ArgbEvaluator().evaluate(percent, unSelectedColor, selectedColor) as Int
        child.findViewById<TextView>(R.id.county)
            .setTextColor(color)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        tabAdapter.onTabClick(null)
        viewPager = null
    }


}

data class TabStyle(@StyleRes val tabTextStyle: Int)

fun getGaussianScale(
    childCenterX: Int,
    minScaleOffest: Float,
    scaleFactor: Float,
    spreadFactor: Double,
    left: Int,
    right: Int
): Float {
    val recyclerCenterX = (left + right) / 2
    return (Math.E.pow(
        -(childCenterX - recyclerCenterX.toDouble()).pow(2.toDouble()) / (2 * spreadFactor.pow(2.toDouble()))
    ) * scaleFactor + minScaleOffest).toFloat()
}
