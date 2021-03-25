package com.louis.app.cavity.ui.bottle.widget

import android.view.animation.Animation
import android.view.animation.Transformation
import com.louis.app.cavity.util.L
import kotlin.math.roundToInt

class GrapeBarAnimation(private val grapeBar: GrapeBar2) : Animation() {
    override fun applyTransformation(interpolatedTime: Float, transformation: Transformation) {
        val grapes = grapeBar.getGrapes()

        grapes.forEach {
            it.qGrape.percentage = (it.qGrape.percentage * interpolatedTime).roundToInt()
        }

        grapeBar.setGrapes(grapes)
        //grapeBar.requestLayout()
    }
}
