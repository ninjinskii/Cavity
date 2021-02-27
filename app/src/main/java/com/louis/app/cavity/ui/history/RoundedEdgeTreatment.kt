package com.louis.app.cavity.ui.history

import com.google.android.material.shape.EdgeTreatment
import com.google.android.material.shape.ShapePath

class RoundedEdgeTreatment(private val radius: Float) : EdgeTreatment(), Cloneable {
    override fun getEdgePath(
        length: Float,
        center: Float,
        interpolation: Float,
        shapePath: ShapePath
    ) {
        val interpolatedRadius = radius * interpolation

        shapePath.lineTo(center - interpolatedRadius, 0f)
        shapePath.addArc(
            center - interpolatedRadius,
            -interpolatedRadius,
            center + interpolatedRadius,
            interpolatedRadius,
            180f,
            180f
        )
        shapePath.lineTo(length, 0f)
    }
}
