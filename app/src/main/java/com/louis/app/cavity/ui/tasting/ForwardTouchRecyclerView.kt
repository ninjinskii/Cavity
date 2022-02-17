package com.louis.app.cavity.ui.tasting

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.ripple.RippleDrawableCompat

/**
 * A RecyclerView that don't dispatch touch events to its children, useful when being used as
 * a nested RecyclerView, without messing up outer RecyclerView items click area
 *
 * Also, it support inner RecyclerView scrolling
 *
 * Takes care of the ripple also
 */
class ForwardTouchRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    RecyclerView(context, attrs, defStyleAttr) {

    private val detector =
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = true

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                forwardTouch(e)
                return true
            }
        })

    private var targetView: View? = null

    fun setTargetView(target: View) {
        targetView = target
    }

    override fun onInterceptTouchEvent(e: MotionEvent) = true

    // There is no interest into clicking here, but we have
    // to intercept touch events to redirect clicks on the underlying view
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        detector.onTouchEvent(e)

        // Let gesture detector handle click case
        if (e.action != MotionEvent.ACTION_UP) {
            forwardTouch(e)
        }

        return super.onTouchEvent(e)
    }

    private fun forwardTouch(e: MotionEvent) {
        val targetBackground = targetView?.background ?: return

        if (targetBackground is RippleDrawableCompat || targetBackground is RippleDrawable) {
            targetBackground.setHotspot(e.x, e.y)

            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    targetBackground.state =
                        intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled)
                }

                MotionEvent.ACTION_UP -> {
                    targetBackground.state = intArrayOf()
                    targetView?.performClick()
                }

                else -> {
                    targetBackground.state = intArrayOf()
                }
            }
        }
    }
}

