package com.louis.app.cavity.ui.search.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Checkable
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.use
import androidx.core.view.postDelayed
import com.louis.app.cavity.R

/**
 * A checkable image button to get rid of weird padding when using default checkboxes
 * due to the empty text beside checkbox's drawable
 */
class AnimatedCheckBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    AppCompatImageButton(context, attrs, defStyleAttr), Checkable, View.OnClickListener {

    private var isChecked = false
    private var lock = false
    private var onCheckedChangeListener: ((Boolean) -> Unit)? = null
    private var delay = 0L

    init {
        this.setOnClickListener(this)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.AnimatedCheckBox,
            defStyleAttr,
            0
        ).use {
            delay = it.getInt(R.styleable.AnimatedCheckBox_delayBeforeRepeat, 0).toLong()
        }
    }

    fun setOnCheckedChangeListener(listener: (Boolean) -> Unit) {
        onCheckedChangeListener = listener
    }

    private fun shouldToggle(): Boolean {
        return if (!lock) {
            lock = true
            postDelayed(delay) { lock = false }
            true
        } else false
    }

    override fun setChecked(checked: Boolean) {
        if (isChecked != checked) {
            isChecked = checked
            refreshDrawableState()
            onCheckedChangeListener?.invoke(isChecked)
        }
    }

    override fun isChecked() = isChecked

    override fun toggle() {
        if (delay <= 0 || shouldToggle()) {
            // WATCH OUT! Do not use property syntax here, it does not work
            setChecked(!isChecked)
        }
    }

    override fun drawableStateChanged() {
        val d = drawable

        if (d != null && d.isStateful && d.setState(drawableState)) {
            invalidateDrawable(d)
        }

        super.drawableStateChanged()
    }

    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        drawable?.jumpToCurrentState()
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked()) {
            mergeDrawableStates(drawableState, intArrayOf(android.R.attr.state_checked))
        }

        return drawableState
    }

    override fun onClick(v: View?) {
        toggle()
    }

    override fun performClick(): Boolean {
        toggle()
        return super.performClick()
    }
}
