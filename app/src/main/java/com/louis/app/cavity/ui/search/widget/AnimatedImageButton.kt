package com.louis.app.cavity.ui.search.widget

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Checkable
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.use
import com.louis.app.cavity.R

class AnimatedImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageButton(context, attrs, defStyleAttr) {

    var state = 0
        private set

    private var initialDrawable: AnimatedVectorDrawable? = null
    private var otherDrawable: AnimatedVectorDrawable? = null
    private var currentUsedDrawable: AnimatedVectorDrawable? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.AnimatedImageButton,
            defStyleAttr,
            0
        ).use {
            initialDrawable =
                it.getDrawable(R.styleable.AnimatedImageButton_initialAvd) as AnimatedVectorDrawable
            otherDrawable =
                it.getDrawable(R.styleable.AnimatedImageButton_otherAvd) as AnimatedVectorDrawable

            setImageDrawable(initialDrawable)
        }
    }

    // TODO: check memory impact
    fun triggerAnimation() {
        if (currentUsedDrawable == null || currentUsedDrawable?.isRunning == false) {
            val baseAvd = if (state == 0) initialDrawable else otherDrawable
            currentUsedDrawable = baseAvd?.constantState?.newDrawable() as AnimatedVectorDrawable
            setImageDrawable(currentUsedDrawable)
            currentUsedDrawable?.start()
            toggleState()
        }
    }

    private fun isAnimationRunning() = currentUsedDrawable?.isRunning ?: false

    private fun toggleState() {
        state = if (state == 1) 0 else 1
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        if (event?.action == MotionEvent.ACTION_UP && !isAnimationRunning()) {
            performClick()
        }

        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        triggerAnimation()
        return true
    }
}

class AnimatedCheckBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageButton(context, attrs, defStyleAttr), Checkable {
    private var isChecked = false

    override fun setChecked(checked: Boolean) {
        if (isChecked != checked) {
            isChecked = checked
            refreshDrawableState()
        }
    }

    override fun isChecked() = isChecked

    override fun toggle() {
        setChecked(!isChecked)
    }

    override fun drawableStateChanged() {
        val d = drawable
        if (d != null && d.isStateful && d.setState(drawableState)) {
            invalidateDrawable(d)
        }

        super.drawableStateChanged()
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked()) {
            mergeDrawableStates(drawableState, intArrayOf(android.R.attr.state_checked))
        }

        return drawableState
    }

    override fun performClick(): Boolean {
        toggle()
        return super.performClick()
    }
}
