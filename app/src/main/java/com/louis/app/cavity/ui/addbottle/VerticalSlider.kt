package com.louis.app.cavity.ui.addbottle

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.slider.Slider

class VerticalSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes : Int = 0
) : Slider(context, attrs, defStyleAttr) {
    init {
        rotation = -90f
    }
}
