package com.louis.app.cavity.ui.stats

import androidx.annotation.ColorRes
import androidx.annotation.StringRes

sealed class StatsUiModel {
    data class Chart(val points: List<ChartPoint>) : StatsUiModel() {
        data class ChartPoint(private val x: Int, private val y: Int)
    }

    data class Pie(val slices: List<PieSlice>) : StatsUiModel() {
        data class PieSlice(@StringRes val name: Int, val angle: Float, @ColorRes val color: Int?)
    }

    data class Mirror(
        val leftSide: Pair<String, Int>,
        val rightSide: Pair<String, Int>
    ) : StatsUiModel()
}
