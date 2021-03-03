package com.louis.app.cavity.ui.home.widget

import com.google.android.material.shape.CornerTreatment
import com.google.android.material.shape.ShapePath

class HexagonalCornerTreatment(private val largerSurface: Float, private val toFlatCorner: Boolean) :
    CornerTreatment(), Cloneable {

    override fun getCornerPath(
        shapePath: ShapePath,
        angle: Float,
        interpolation: Float,
        radius: Float
    ) {
        val interpolatedRadius = radius * interpolation
        val flatCornerPart = largerSurface / 4
        val firstLine = if (toFlatCorner) flatCornerPart to 0f else 0f to flatCornerPart

        shapePath.reset(0f, interpolatedRadius)
        shapePath.lineTo(firstLine.first, firstLine.second)
        shapePath.lineTo(interpolatedRadius, 0f)
    }

    /*
        flatCornerPart
        \____________/
          |    |
      ____|____|
     /         \
    /           \
    \           /
     \_________/

    _____________
    |           |
    |           |
    _____________
    /            \
    largerSurface
     */
}
