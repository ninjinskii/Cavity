package com.louis.app.cavity.ui.stats

import com.louis.app.cavity.domain.stats.StatsYearTimeSpan
import com.louis.app.cavity.domain.stats.Stat
import com.louis.app.cavity.domain.stats.StatGroupBy
import com.louis.app.cavity.domain.stats.InventoryStatFilter
import com.louis.app.cavity.domain.stats.StatsQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FlowStatsHelper(private val statQueries: StatsQueries) {
    fun getResults(
        statGroupBy: StatGroupBy,
        timeSpan: StatsYearTimeSpan,
        inventoryStatFilter: InventoryStatFilter,
        includeGifts: Boolean
    ): Flow<List<Stat>> =
        getStat(
            statGroupBy = statGroupBy,
            statsTimeSpan = timeSpan,
            inventoryStatFilter = inventoryStatFilter,
            includeGifts = includeGifts
        )

    fun getComparisons(
        statGroupBy: StatGroupBy,
        comparisonStatsTimeSpan: StatsYearTimeSpan,
        inventoryStatFilter: InventoryStatFilter,
        includeGifts: Boolean
    ): Flow<List<Stat>> =
        getStat(
            statGroupBy = statGroupBy,
            statsTimeSpan = comparisonStatsTimeSpan,
            inventoryStatFilter = inventoryStatFilter,
            includeGifts = includeGifts
        )

    private fun getStat(
        statGroupBy: StatGroupBy,
        statsTimeSpan: StatsYearTimeSpan,
        inventoryStatFilter: InventoryStatFilter,
        includeGifts: Boolean
    ): Flow<List<Stat>> {

        val start = statsTimeSpan.yearStart
        val end = statsTimeSpan.yearEnd

        return when (inventoryStatFilter) {
            is InventoryStatFilter.Stock -> statGroupBy.stockQuery(statQueries)
            else ->
                statQueries
                    .getStatsByHistoryEntry(
                        start,
                        end,
                        inventoryStatFilter.getHistoryEntryTypes(includeGifts),
                        statGroupBy.groupBy
                    )
                    .map(statGroupBy.postProcess)
        }
    }
}
