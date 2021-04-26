package com.louis.app.cavity.ui.stats

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.BoundedHistoryEntry
import com.louis.app.cavity.db.dao.ColorStat
import com.louis.app.cavity.db.dao.VintageStat
import com.louis.app.cavity.db.dao.Year
import com.louis.app.cavity.util.ColorUtil
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.L
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val year = MutableLiveData(DateFormatter.getCurrentYear())
    private val statType = MutableLiveData(StatType.STOCK)

    val years = repository.getYears()

//    val replenishmentsByColor = year.switchMap { repository.getReplenishmentsByColor(it.first, it.second) }

    val colorStats = MediatorLiveData<List<ColorStat>>().apply {
        addSource(year) {
            fetchColorStats(it, statType.value!!)
        }
        addSource(statType) {
            fetchColorStats(year.value!!, it)
        }
    }

    val vintageStats = MediatorLiveData<List<VintageStat>>().apply {
        addSource(year) {
            fetchVintageStats(it, statType.value!!)
        }
        addSource(statType) {
            fetchVintageStats(year.value!!, it)
        }
    }

//    val colorStats = year.combine(statType) { year, statType ->
//        fetchColorStats(year, statType)
//    }

    private val entries = year.switchMap {
        repository.getBoundedEntriesBetween(it.first, it.second)
    }

    private val replenishments = entries.switchMap { filterReplenishments(it) }
    private val consumptions = entries.switchMap { filterConsumptions(it) }

    //val display = entries.switchMap { prepareResults(it) }

    private fun fetchColorStats(year: Pair<Long, Long>, statType: StatType) {
        L.v("year: $year")
        viewModelScope.launch(IO) {
            val result = when (statType) {
                StatType.STOCK -> repository.getStockByColor()
                StatType.REPLENISHMENTS -> repository.getReplenishmentsByColor(
                    year.first,
                    year.second
                )
                StatType.CONSUMPTIONS -> repository.getConsumptionsByColor(year.first, year.second)
            }

            colorStats.postValue(result)
        }
    }

    private fun fetchVintageStats(year: Pair<Long, Long>, statType: StatType) {
        viewModelScope.launch(IO) {
            val result = when (statType) {
                StatType.STOCK -> repository.getStockByVintage()
                StatType.REPLENISHMENTS -> repository.getReplenishmentsByVintage(
                    year.first,
                    year.second
                )
                StatType.CONSUMPTIONS -> repository.getConsumptionsByVintage(
                    year.first,
                    year.second
                )
            }

            vintageStats.postValue(result)
        }
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

    fun setYear(year: Year) {
        this.year.value = year.yearStart to year.yearEnd
    }

    fun setStatType(statType: StatType) {
        this.statType.value = statType
    }
}

enum class StatType {
    STOCK,
    REPLENISHMENTS,
    CONSUMPTIONS,
}


