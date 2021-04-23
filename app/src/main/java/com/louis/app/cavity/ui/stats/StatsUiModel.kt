package com.louis.app.cavity.ui.stats

sealed class StatsUiModel {
    data class Chart(val points: List<ChartPoint>) : StatsUiModel()
    data class Pie(val data: Stat) : StatsUiModel()
    data class Mirror(
        val leftSide: Pair<String, Int>,
        val rightSide: Pair<String, Int>
    ) : StatsUiModel()
}
