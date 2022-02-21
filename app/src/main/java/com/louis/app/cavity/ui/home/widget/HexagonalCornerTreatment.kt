package com.louis.app.cavity.ui.home.widget

import com.google.android.material.shape.CornerTreatment
import com.google.android.material.shape.ShapePath

class HexagonalCornerTreatment(private val toFlatCorner: Boolean) : CornerTreatment(), Cloneable {

    companion object {
        private const val HEXAGONAL_SQUARE_RATIO = 0.866f
    }

    override fun getCornerPath(
        shapePath: ShapePath,
        angle: Float,
        interpolation: Float,
        radius: Float
    ) {
        val interpolatedRadius = radius * interpolation
        val length = (interpolatedRadius * 2) / HEXAGONAL_SQUARE_RATIO
        val flatCornerPart = length / 4
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
   /             \
    largerSurface
     */
}
