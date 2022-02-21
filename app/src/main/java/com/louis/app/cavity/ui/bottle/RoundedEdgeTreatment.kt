package com.louis.app.cavity.ui.bottle

import com.google.android.material.shape.EdgeTreatment
import com.google.android.material.shape.ShapePath

class RoundedEdgeTreatment(private val height: Float) : EdgeTreatment() {
    override fun getEdgePath(
        length: Float,
        center: Float,
        interpolation: Float,
        shapePath: ShapePath
    ) {
        val interpolatedArcHeight = height * 2 * interpolation
        shapePath.quadToPoint(length / 2, interpolatedArcHeight, length, 0f)
    }
}
