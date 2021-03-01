package com.louis.app.cavity.ui.history

import com.google.android.material.shape.EdgeTreatment
import com.google.android.material.shape.ShapePath

class BinderEdgeTreatment(private val diameter: Float) : EdgeTreatment(), Cloneable {
    override fun getEdgePath(
        length: Float,
        center: Float,
        interpolation: Float,
        shapePath: ShapePath
    ) {
        val interpolatedDiameter = diameter * interpolation

        shapePath.addArc(
            0f,
            0f,
            interpolatedDiameter,
            interpolatedDiameter,
            180f,
            180f
        )
        shapePath.lineTo(length, interpolatedDiameter / 2)
    }
}
