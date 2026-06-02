package com.louis.app.cavity.ui.stats

import androidx.annotation.StringRes
import com.louis.app.cavity.R
import com.louis.app.cavity.db.dao.BaseStat
import com.louis.app.cavity.db.dao.Stat
import com.louis.app.cavity.db.dao.WineColorStat
import com.louis.app.cavity.db.dao.Year
import com.louis.app.cavity.domain.history.HistoryEntryType
import com.louis.app.cavity.domain.history.toInt
import com.louis.app.cavity.domain.repository.StatsRepository
import com.louis.app.cavity.model.WineColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class FlowStatsFactory(
    private val repository: StatsRepository,
    private val year: MutableStateFlow<Year>,
    private val comparisonYear: MutableStateFlow<Year>
) {
    private val statRequests = MutableList(4) {
        MutableStateFlow(StatRequest(StatType.STOCK, false))
    }

    fun getResults(position: Int): Flow<List<Stat>> = createFlowStat(position)

    fun getComparisons(position: Int): Flow<List<Stat>> = createComparisonFlowStat(position)

    @StringRes
    fun getStatTypeLabel(position: Int): Int {
        return when (statRequests[position].value.statType) {
            StatType.STOCK -> R.string.stock
            StatType.REPLENISHMENTS -> R.string.replenishments
            StatType.CONSUMPTIONS -> R.string.consumptions
        }
    }

    fun applyStatType(position: Int, statType: StatType) {
        statRequests[position].value = statRequests[position].value.copy(statType = statType)
    }

    fun applyIncludeGifts(position: Int, includeGifts: Boolean) {
        statRequests[position].value = statRequests[position].value.copy(includeGifts = includeGifts)
    }

    private fun createFlowStat(position: Int) = combine(year, statRequests[position]) { y, req -> y to req }
        .flatMapLatest { (y, req) -> getStat(position, y, req.statType, req.includeGifts) }

    private fun createComparisonFlowStat(position: Int) = combine(comparisonYear, statRequests[position]) { y, req -> y to req }
        .flatMapLatest { (y, req) -> getStat(position, y, req.statType, req.includeGifts) }

    private fun getStat(
        position: Int,
        year: Year,
        statType: StatType,
        includeGifts: Boolean
    ): Flow<List<Stat>> {
        val start = year.yearStart
        val end = year.yearEnd
        val types = when (statType) {
            StatType.STOCK -> emptyList()
            StatType.REPLENISHMENTS -> mutableListOf(HistoryEntryType.ADD.toInt()).also {
                if (includeGifts) it.add(HistoryEntryType.GIVEN_BY.toInt())
            }
            StatType.CONSUMPTIONS -> mutableListOf(
                HistoryEntryType.REMOVE.toInt(),
                HistoryEntryType.TASTING.toInt()
            ).also {
                if (includeGifts) it.add(HistoryEntryType.GIFTED_TO.toInt())
            }
        }

        val groupBy = when (position) {
            0 -> "county.name"
            1 -> "wine.color"
            2 -> "bottle.vintage"
            else -> "wine.naming"
        }

        val mapToWineColor = position == 1
        val stockFunctions = listOf(
            { repository.getStockByCounty() },
            { repository.getStockByColor() },
            { repository.getStockByVintage() },
            { repository.getStockByNaming() }
        )

        @Suppress("UNCHECKED_CAST")
        return when (statType) {
            StatType.STOCK -> stockFunctions.getOrElse(position) { stockFunctions.last() }.invoke() as Flow<List<Stat>>
            StatType.REPLENISHMENTS, StatType.CONSUMPTIONS ->
                repository.getStatsByHistoryEntry(start, end, types, groupBy)
                    .map { if (mapToWineColor) mapToWineColor(it) else it as List<Stat> }
        }
    }

    private fun mapToWineColor(stats: List<BaseStat>): List<Stat> {
        return stats.map {
            WineColorStat(WineColor.valueOf(it.label), it.count, it.percentage, it.bottleIds)
        }
    }

    data class StatRequest(val statType: StatType, val includeGifts: Boolean)
}
