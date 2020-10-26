package com.louis.app.cavity.ui.bottle

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.appcompat.view.ContextThemeWrapper
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

    private val binding: GrapeBarBinding
    private val bars = mutableListOf<Pair<ProgressBar, Int>>()
    private val colors = mutableListOf(
        context.getColor(R.color.cavity_red),
        context.getColor(R.color.cavity_indigo),
        context.getColor(R.color.cavity_yellow),
        context.getColor(R.color.cavity_light_green),
        context.getColor(R.color.cavity_purple),
        context.getColor(R.color.cavity_brown),
        context.getColor(R.color.cavity_grey),
    )

    init {
        val view = inflate(context, R.layout.grape_bar, this)
        binding = GrapeBarBinding.bind(view)
    }

    fun addAllGrapes(grapes: List<Grape>) {
        val mutGrapes = grapes.toMutableList()
        mutGrapes.sortBy { it.percentage }
        mutGrapes.forEachIndexed { index, grape -> prepareBar(index, grape) }
    }

    fun triggerAnimation() {
        var progress = 0

        bars.reverse()
        bars.forEach {
            ObjectAnimator.ofInt(it.first, "progress", 0, (it.second + progress) * 10).apply {
                duration = 800
                start()
            }
            progress += it.second
        }

        ObjectAnimator.ofInt(binding.placeholder, "progress", 0, 1000).apply {
            duration = 800
            start()
        }
    }

    private fun prepareBar(index: Int, grape: Grape) {
        val set = ConstraintSet()
        val progressBar = ProgressBar(
            ContextThemeWrapper(
                context,
                R.style.Widget_AppCompat_ProgressBar_Horizontal
            ), null, 0
        )

        progressBar.apply {
            max = 1000
            isIndeterminate = false
            id = generateViewId()
            progressBackgroundTintList =
                ColorStateList.valueOf(context.getColor(android.R.color.transparent))
            progressTintList = ColorStateList.valueOf(colors[index])
        }

        bars.add(progressBar to grape.percentage)
        addView(progressBar, childCount)

        set.apply {
            clone(this)
            connect(progressBar.id, ConstraintSet.START, id, ConstraintSet.START)
            connect(progressBar.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
            connect(progressBar.id, ConstraintSet.END, id, ConstraintSet.END)
            connect(progressBar.id, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
            applyTo(this@GrapeBar)
        }
    }
}
