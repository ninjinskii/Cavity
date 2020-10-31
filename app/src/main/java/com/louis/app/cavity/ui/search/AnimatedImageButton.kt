package com.louis.app.cavity.ui.search

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.getResourceIdOrThrow
import com.louis.app.cavity.R
import com.louis.app.cavity.util.L
import java.security.InvalidParameterException

class AnimatedImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageButton(context, attrs, defStyleAttr) {

    private var state = 0
    @DrawableRes
    private val initialDrawable: Int
    @DrawableRes
    private val otherDrawable: Int

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.AnimatedImageButton,
            defStyleAttr,
            0
        ).apply {
            try {
                initialDrawable = getResourceIdOrThrow(R.styleable.AnimatedImageButton_initialAvd)
                otherDrawable = getResourceIdOrThrow(R.styleable.AnimatedImageButton_otherAvd)
                setImageResource(initialDrawable)
                setOnClickListener {
                    triggerAnimation()
                }
            } catch (e: ClassCastException) {
                throw InvalidParameterException("Cannot convert the given drawables into AnimatedVectorDrawable")
            } finally {
                recycle()
            }
        }
    }

    private fun triggerAnimation() {
        val avd = if (state == 1) initialDrawable else otherDrawable
        L.v(state.toString())
        setImageResource(avd)
        (drawable as AnimatedVectorDrawable).start()
        state = if (state == 1) 0 else 1
    }
}
