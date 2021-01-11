package com.louis.app.cavity.ui.search.widget

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.getDrawableOrThrow
import com.louis.app.cavity.R

class AnimatedImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageButton(context, attrs, defStyleAttr) {

    var state = 0
        private set

    private var initialDrawable: AnimatedVectorDrawable
    private var otherDrawable: AnimatedVectorDrawable
    private var currentUsedDrawable: AnimatedVectorDrawable? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.AnimatedImageButton,
            defStyleAttr,
            0
        ).apply {
            try {
                initialDrawable = getDrawableOrThrow(
                    R.styleable.AnimatedImageButton_initialAvd
                ) as AnimatedVectorDrawable
                otherDrawable = getDrawableOrThrow(
                    R.styleable.AnimatedImageButton_otherAvd
                ) as AnimatedVectorDrawable

                setImageDrawable(initialDrawable)
            } catch (e: ClassCastException) {
                throw IllegalArgumentException("Cannot convert the given drawables into AnimatedVectorDrawable")
            } catch (e: Resources.NotFoundException) {
                throw IllegalArgumentException("Attributes initialAvd & otherAvd must be Drawables")
            } finally {
                recycle()
            }
        }
    }

    // TODO: check memory impact
    fun triggerAnimation() {
        if (currentUsedDrawable == null || currentUsedDrawable?.isRunning == false) {
            val baseAvd = if (state == 0) initialDrawable else otherDrawable
            currentUsedDrawable = baseAvd.constantState?.newDrawable() as AnimatedVectorDrawable
            setImageDrawable(currentUsedDrawable)
            currentUsedDrawable?.start()
            toggleState()
        }
    }

    fun isAnimationRunning() = currentUsedDrawable?.isRunning ?: false

    private fun toggleState() {
        state = if (state == 1) 0 else 1
    }
}
