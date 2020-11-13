package com.louis.app.cavity.ui.home.widget

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.shape.MaterialShapeDrawable
import com.louis.app.cavity.R
import com.louis.app.cavity.model.County
import com.louis.app.cavity.util.L
import kotlin.math.pow

class CountyScrollableTab @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val tabAdapter by lazy { TabAdapter(style = tabTextStyle) }
    private var selectedColor = Color.WHITE
    private var unSelectedColor = Color.GRAY
    private var tabTextStyle = TabStyle(R.style.TabTextAppearance)
    private val layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
    private var viewPager: ViewPager2? = null
    private var isRVScrolling = true
    private var listener: ((position: Int) -> Unit)? = null
    private var pageChangeListener: ((position: Int) -> Unit)? = null

    init {
        initAttributes(attrs)

        setLayoutManager(layoutManager)
        setHasFixedSize(true)
        adapter = tabAdapter

        val snapHelper = LinearSnapHelper()
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

    // We want to use touch listener as an indicator that the recyclerview might be scrolled
    @SuppressLint("ClickableViewAccessibility")
    private fun swallowTouchEvents() {
        setOnTouchListener { _, _ ->
            isRVScrolling = true
            false
        }
    }

    fun addTabs(list: List<County>) {
        tabAdapter.addAll(list)
    }

    fun addOnLongClickListener(longClickListener: (county: County) -> Unit) {
        tabAdapter.onLongTabClick(longClickListener)
    }

    fun addOnTabListener(listener: (position: Int) -> Unit) {
        this.listener = listener
    }

    fun addOnPageChangeListener(pageChangeListener: (position: Int) -> Unit) {
        this.pageChangeListener = pageChangeListener
    }

    fun setUpWithViewPager(viewPager: ViewPager2) {
        if (viewPager.adapter == null) throw IllegalStateException(
            "ViewPager does not have pager adapter"
        )
        this.viewPager = viewPager

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING)
                    isRVScrolling = false
            }

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

    private fun colorView(child: View, scaleValue: Float) {
        val percent = (scaleValue - 1) / 1f
        val color = ArgbEvaluator().evaluate(percent, unSelectedColor, selectedColor) as Int
        child.findViewById<TextView>(R.id.county)
            .setTextColor(color)
    }

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
            -(childCenterX - recyclerCenterX.toDouble()).pow(2.toDouble()) / (2 * spreadFactor.pow(
                2.toDouble()
            ))
        ) * scaleFactor + minScaleOffest).toFloat()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        tabAdapter.onTabClick(null)
        tabAdapter.onLongTabClick(null)
        viewPager = null
    }
}

data class TabStyle(@StyleRes val tabTextStyle: Int)
