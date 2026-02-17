package com.louis.app.cavity.ui.stats

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.louis.app.cavity.R
import com.louis.app.cavity.db.dao.BaseStat
import com.louis.app.cavity.db.dao.Stat
import com.louis.app.cavity.db.dao.WineColorStat
import com.louis.app.cavity.db.dao.Year
import com.louis.app.cavity.domain.history.HistoryEntryType
import com.louis.app.cavity.domain.history.toInt
import com.louis.app.cavity.domain.repository.StatsRepository
import com.louis.app.cavity.model.WineColor

class LiveDataStatsFactory(
    private val repository: StatsRepository,
    private val year: MutableLiveData<Year>,
    private val comparisonYear: MutableLiveData<Year>
) {
    private val statRequests = MutableList(4) {
        MutableLiveData(StatRequest(StatType.STOCK, false))
    }

    private val _comparisons: MutableList<LiveData<List<Stat>>> = MutableList(4) {
        createComparisonLiveStat(it)
    }
    val comparisons: List<LiveData<List<Stat>>>
        get() = _comparisons

    private val _results: MutableList<LiveData<List<Stat>>> = MutableList(4) {
        createLiveStat(it)
    }
    val results: List<LiveData<List<Stat>>>
        get() = _results

    @StringRes
    fun getStatTypeLabel(position: Int): Int {
        return when (statRequests[position].value?.statType) {
            StatType.STOCK -> R.string.stock
            StatType.REPLENISHMENTS -> R.string.replenishments
            StatType.CONSUMPTIONS -> R.string.consumptions
            else -> -1
        }
    }

    fun applyStatType(position: Int, statType: StatType) {
        statRequests[position].value = statRequests[position].value?.copy(statType = statType)
    }

    fun applyIncludeGifts(position: Int, includeGifts: Boolean) {
        statRequests[position].value =
            statRequests[position].value?.copy(includeGifts = includeGifts)
    }

    private fun createLiveStat(position: Int) = year.switchMap { year ->
        statRequests[position].switchMap { statRequest ->
            getStat(position, year, statRequest.statType, statRequest.includeGifts)
        }
    }

    private fun createComparisonLiveStat(position: Int) = comparisonYear.switchMap { comparisonY ->
        statRequests[position].switchMap { statRequest ->
            getStat(position, comparisonY, statRequest.statType, statRequest.includeGifts)
        }
    }

    private fun getStat(
        position: Int,
        year: Year,
        statType: StatType,
        includeGifts: Boolean
    ): LiveData<List<Stat>> {
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

        return when (statType) {
            StatType.STOCK -> stockFunctions.getOrElse(position) { stockFunctions.last() }.invoke()
            StatType.REPLENISHMENTS -> repository.getStatsByHistoryEntry(start, end, types, groupBy)
                .map { if (mapToWineColor) mapToWineColor(it) else it }

            StatType.CONSUMPTIONS -> repository.getStatsByHistoryEntry(start, end, types, groupBy)
                .map { if (mapToWineColor) mapToWineColor(it) else it }
        } as LiveData<List<Stat>>
    }

    private fun mapToWineColor(stats: List<BaseStat>): List<Stat> {
        return stats.map {
            WineColorStat(WineColor.valueOf(it.label), it.count, it.percentage, it.bottleIds)
        }
    }

    data class StatRequest(val statType: StatType, val includeGifts: Boolean)
}
