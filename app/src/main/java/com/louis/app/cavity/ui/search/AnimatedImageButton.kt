package com.louis.app.cavity.ui.search

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Animatable
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import com.louis.app.cavity.R
import com.louis.app.cavity.util.L
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.InvalidParameterException

class AnimatedImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageButton(context, attrs, defStyleAttr) {

    var state = 0
        private set

    private val initialDrawable: AnimatedVectorDrawable
    private val otherDrawable: AnimatedVectorDrawable
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

    private fun toggleState() {
        state = if (state == 1) 0 else 1
    }
}
