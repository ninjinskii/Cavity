package com.louis.app.cavity.ui.home

import com.google.android.material.shape.CornerTreatment
import com.google.android.material.shape.ShapePath
import com.louis.app.cavity.util.L

class HexagonalToPointCornerTreatment(private val surfaceLength: Float) : CornerTreatment(), Cloneable {
    override fun getCornerPath(
        shapePath: ShapePath,
        angle: Float,
        interpolation: Float,
        radius: Float
    ) {
        L.v("angle: $angle, radius: $radius")


        shapePath.reset(0f, radius)
        shapePath.lineTo(0f, (surfaceLength / 2) - 25f) //25f = 1/2 S
        shapePath.lineTo(radius, 0f)
    }
}

class HexagonalToSideCornerTreatment(private val surfaceLength: Float) : CornerTreatment(), Cloneable {
    override fun getCornerPath(
        shapePath: ShapePath,
        angle: Float,
        interpolation: Float,
        radius: Float
    ) {
        L.v("angle: $angle, radius: $radius")


        shapePath.reset(0f, radius)
        shapePath.lineTo(radius, 0f)
    }
}
