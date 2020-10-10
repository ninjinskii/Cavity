package com.louis.app.cavity.ui.bottle

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.GrapeBarBinding
import com.louis.app.cavity.model.Grape

class GrapeBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_ROTATION = 310
    }

    private val binding: GrapeBarBinding
    private val bars = mutableListOf<ProgressBar>()
    private var rotation: Int

    init {
        val view = inflate(context, R.layout.grape_bar, this)
        binding = GrapeBarBinding.bind(view)

        context.theme.obtainStyledAttributes(attrs, R.styleable.GrapeBar, 0, 0).apply {
            try {
                rotation = getInt(R.styleable.GrapeBar_textRotation, DEFAULT_ROTATION)
            } finally {
                recycle()
            }
        }
    }

    fun setTextRotation(rotation: Int) {
        this.rotation = rotation
        invalidate()
        requestLayout()
    }

    fun getTextRotation() = rotation

    fun addAllGrapes(vararg grapes: Grape) {
        grapes.forEach { addBar(it) }
        invalidate()
        requestLayout()
        bars.forEach{ it.progress = 10 }
    }

    fun triggerAnimation() {
        bars.forEach {
            // remove setProgresse in addBar, and set MutableList bars to type Pair<ProgressBar, Int> whene Int is the progress
            // then animate to progress
        }
    }

    private fun addBar(grape: Grape) {
        val set = ConstraintSet()
        val progressBar = ProgressBar(context)
        progressBar.progress = grape.percentage
        progressBar.id = generateViewId()
        bars.add(progressBar)
        addView(progressBar, 0)
        set.apply {
            clone(this)
            connect(progressBar.id, ConstraintSet.START, id, ConstraintSet.START)
            connect(progressBar.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
            applyTo(this@GrapeBar)
        }
    }
}
