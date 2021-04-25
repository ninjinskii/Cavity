package com.louis.app.cavity.ui.stats

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.BoundedHistoryEntry
import com.louis.app.cavity.db.dao.IntStat
import com.louis.app.cavity.util.ColorUtil
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.L
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val currentYear = DateFormatter.getYearBounds(System.currentTimeMillis())

    private val year = MutableLiveData(currentYear)
    private val statType = MutableLiveData(StatType.STOCK)

    val colorStats = MediatorLiveData<List<IntStat>>().apply {
        addSource(year) {
            fetchColorStats(it, statType.value!!)
        }
        addSource(statType) {
            fetchColorStats(year.value!!, it)
        }
    }

//    val colorStats = year.combine(statType) { year, statType ->
//        fetchColorStats(year, statType)
//    }

    private val entries = year.switchMap {
        repository.getBoundedEntriesBetween(it.first, it.second)
    }

    val years = repository.getYears().switchMap { getYears(it) }
    private val replenishments = entries.switchMap { filterReplenishments(it) }
    private val consumptions = entries.switchMap { filterConsumptions(it) }

    //val display = entries.switchMap { prepareResults(it) }

    private fun getYears(timestamps: List<Long>) = liveData(Default) {
        emit(
            timestamps.map { DateFormatter.formatDate(it, pattern = "yyyy") }.distinct()
        )
    }

    private fun fetchColorStats(year: Pair<Long, Long>, statType: StatType) = liveData(IO) {
        L.v("statType: ${statType.name}")
        emit(
            when (statType) {
                StatType.STOCK -> repository.getStockByColor()
                StatType.REPLENISHMENTS -> repository.getReplenishmentsByColor(
                    year.first,
                    year.second
                )
                StatType.CONSUMPTIONS -> repository.getConsumptionsByColor(year.first, year.second)
            }
        )
    }


//    private fun getBottlesByCounty(entries: List<BoundedHistoryEntry>) = liveData(Default) {
//        val grouped = entries
//            .sortedBy { it.wineAndCounty.county.prefOrder }
//            .groupBy { it.wineAndCounty.county.id }
//
//        emit(grouped.keys.map {
//            StringStatItem(
//                name = grouped[it]!!.first().wineAndCounty.county.name,
//                count = grouped[it]!!.size,
//                color = null,
//                icon = null
//            )
//        })
//    }


    private fun getBottlesByColor(entries: List<BoundedHistoryEntry>) = liveData(Default) {
        val grouped = entries
            .sortedBy { it.bottleAndWine.wine.color }
            .groupBy { it.bottleAndWine.wine.color }

        emit(Stat(grouped.keys.map {
            StringResStatItem(
                name = ColorUtil.getStringResForWineColor(it),
                count = grouped[it]!!.size,
                color = ColorUtil.getColorResForWineColor(it),
                icon = null
            )
        }))
    }

    private fun getConsumedBottlesByVintage(
        entries: List<BoundedHistoryEntry>
    ): List<StatItem> {
        val grouped = entries
            .filter { it.historyEntry.type == 0 }
            .sortedBy { it.bottleAndWine.bottle.vintage }
            .groupBy { it.bottleAndWine.bottle.vintage }

        return grouped.keys.map {
            StringStatItem(
                name = it.toString(),
                count = grouped[it]!!.size,
                color = null,
                icon = null
            )
        }
    }

    private fun filterReplenishments(entries: List<BoundedHistoryEntry>) = liveData(Default) {
        emit(entries.filter { it.historyEntry.type == 1 && it.historyEntry.type == 3 })
    }

    private fun filterConsumptions(entries: List<BoundedHistoryEntry>) = liveData(Default) {
        emit(entries.filter { it.historyEntry.type == 0 && it.historyEntry.type == 2 })
    }

    fun setYear(timestamp: Long?) {
        L.v("setYear: $timestamp")
        year.value = if (timestamp != null) {
            DateFormatter.getYearBounds(timestamp)
        } else {
            0L to System.currentTimeMillis()
        }
    }

    fun setStatType(statType: StatType) {
        L.v("statType: ${statType.name}")
        this.statType.value = statType
    }
}

enum class StatType {
    STOCK,
    REPLENISHMENTS,
    CONSUMPTIONS,
}


