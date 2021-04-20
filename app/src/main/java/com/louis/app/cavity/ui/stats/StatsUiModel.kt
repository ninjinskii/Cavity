package com.louis.app.cavity.ui.stats

import androidx.annotation.ColorRes

sealed class StatsUiModel {
    data class Chart(val points: List<ChartPoint>) : StatsUiModel() {
        data class ChartPoint(private val x: Int, private val y: Int)
    }

    data class Pie(val slices: List<PieSlice>) : StatsUiModel() {
        // To compute angle: (380 * percentage) / 100
        data class PieSlice(val name: String, val angle: Float, @ColorRes val color: Int?)
    }

    data class Mirror(
        val leftSide: Pair<String, Int>,
        val rightSide: Pair<String, Int>
    ) : StatsUiModel()
}
