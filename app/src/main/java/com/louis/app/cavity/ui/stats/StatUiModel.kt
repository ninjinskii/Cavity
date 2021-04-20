package com.louis.app.cavity.ui.stats

import androidx.annotation.ColorRes

sealed class StatUiModel {
    data class Chart(val points: List<ChartPoint>) : StatUiModel() {
        data class ChartPoint(private val x: Int, private val y: Int)
    }

    data class Pie(val slices: List<PieSlice>) : StatUiModel() {
        // To compute angle: (380 * percentage) / 100
        data class PieSlice(val name: String, val angle: Int, @ColorRes val color: Int?)
    }

    data class Mirror(
        val leftSide: Pair<String, Int>,
        val rightSide: Pair<String, Int>
    ) : StatUiModel()
}
