package com.louis.app.cavity.ui.stats

import androidx.annotation.StringRes
import com.louis.app.cavity.R
import com.louis.app.cavity.db.dao.Year
import com.louis.app.cavity.domain.stats.Stat
import com.louis.app.cavity.domain.stats.StatSlot
import com.louis.app.cavity.domain.stats.StatType
import com.louis.app.cavity.domain.stats.StatsQueries
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class FlowStatsFactory(
    private val statQueries: StatsQueries,
    private val year: MutableStateFlow<Year>,
    private val comparisonYear: MutableStateFlow<Year>
) {
    private val statRequests = mapOf(
        StatSlot.COUNTY to MutableStateFlow(StatRequest(StatType.STOCK, false)),
        StatSlot.COLOR to MutableStateFlow(StatRequest(StatType.STOCK, false)),
        StatSlot.VINTAGE to MutableStateFlow(StatRequest(StatType.STOCK, false)),
        StatSlot.NAMING to MutableStateFlow(StatRequest(StatType.STOCK, false))
    )

    fun getResults(statSlot: StatSlot): Flow<List<Stat>> = createFlowStat(statSlot)

    fun getComparisons(statSlot: StatSlot): Flow<List<Stat>> = createComparisonFlowStat(statSlot)

    @StringRes
    fun getStatTypeLabel(statSlot: StatSlot): Int {
        return when (statFor(statSlot).value.statType) {
            StatType.STOCK -> R.string.stock
            StatType.REPLENISHMENTS -> R.string.replenishments
            StatType.CONSUMPTIONS -> R.string.consumptions
        }
    }

    fun applyStatType(statSlot: StatSlot, statType: StatType) {
        statFor(statSlot).value = statFor(statSlot).value.copy(statType = statType)
    }

    fun applyIncludeGifts(statSlot: StatSlot, includeGifts: Boolean) {
        statFor(statSlot).value = statFor(statSlot).value.copy(includeGifts = includeGifts)
    }

    fun getStatType(statSlot: StatSlot) = statFor(statSlot).value.statType

    private fun statFor(statSlot: StatSlot) = statRequests[statSlot]!!

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createFlowStat(statSlot: StatSlot) =
        combine(year, statFor(statSlot)) { y, req -> y to req }
            .flatMapLatest { (y, req) -> getStat(statSlot, y, req.statType, req.includeGifts) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createComparisonFlowStat(statSlot: StatSlot) =
        combine(comparisonYear, statFor(statSlot)) { y, req -> y to req }
            .flatMapLatest { (y, req) -> getStat(statSlot, y, req.statType, req.includeGifts) }

    private fun getStat(
        statSlot: StatSlot,
        year: Year,
        statType: StatType,
        includeGifts: Boolean
    ): Flow<List<Stat>> {

        val start = year.yearStart
        val end = year.yearEnd

        return when (statType) {
            StatType.STOCK -> statSlot.stockQuery(statQueries)
            else ->
                statQueries
                    .getStatsByHistoryEntry(
                        start,
                        end,
                        statType.buildTypes(includeGifts),
                        statSlot.groupBy
                    )
                    .map(statSlot.postProcess)
        }
    }

    data class StatRequest(val statType: StatType, val includeGifts: Boolean)
}
