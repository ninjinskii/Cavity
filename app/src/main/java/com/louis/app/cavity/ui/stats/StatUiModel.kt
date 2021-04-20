package com.louis.app.cavity.ui.stats

sealed class StatUiModel {
    data class Chart(val points: List<ChartPoint>) : StatUiModel() {
        data class ChartPoint(private val x: Int, private val y: Int)
    }

    data class Pie(val slices: List<PieSlice>) : StatUiModel() {
        data class PieSlice(private val name: String, private val percentage: Int)
    }

    data class Mirror(
        val leftSide: Pair<String, Int>,
        val rightSide: Pair<String, Int>
    ) : StatUiModel()
}
