package com.louis.app.cavity.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.BoundedHistoryEntry
import com.louis.app.cavity.ui.stats.StatsViewModel.StatType.CONSUMED_BOTTLES_BY_COLORS
import com.louis.app.cavity.ui.stats.StatsViewModel.StatType.CONSUMED_BOTTLES_BY_VINTAGE
import com.louis.app.cavity.util.ColorUtil
import com.louis.app.cavity.util.DateFormatter
import kotlinx.coroutines.Dispatchers.Default

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val currentYear = DateFormatter.getYearBounds(System.currentTimeMillis())
    private val year = MutableLiveData(currentYear)

    private val entries = year.switchMap {
        repository.getBoundedEntriesBetween(it.first, it.second)
    }

    val display = entries.switchMap { prepareResults(it) }

    fun getStat(stat: StatType) = liveData(Default) {
        val entries = entries.value ?: emptyList()

        emit(
            when (stat) {
                CONSUMED_BOTTLES_BY_COLORS -> getConsumedBottlesByColor(entries)
                CONSUMED_BOTTLES_BY_VINTAGE -> getConsumedBottlesByVintage(entries)
            }
        )
    }

    private fun prepareResults(entries: List<BoundedHistoryEntry>) = liveData(Default) {
// move entires.filter here eventually
        emit(
            listOf(
                StatsUiModel.Pie(Stat(getConsumedBottlesByColor(entries))),
                StatsUiModel.Pie(Stat(getConsumedBottlesByVintage(entries)))
            )
        )
    }

    private fun getConsumedBottlesByColor(
        entries: List<BoundedHistoryEntry>
    ): List<StatItem> {
        val grouped = entries
            .filter { it.historyEntry.type == 0 }
            .sortedBy { it.bottleAndWine.wine.color }
            .groupBy { it.bottleAndWine.wine.color }

        return grouped.keys.map {
            StringResStatItem(
                name = ColorUtil.getStringResForWineColor(it),
                count = grouped[it]!!.size,
                color = ColorUtil.getColorResForWineColor(it),
                icon = null
            )
        }
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

    fun setYear(timestamp: Long?) {
        year.value = if (timestamp != null) {
            DateFormatter.getYearBounds(timestamp)
        } else {
            0L to System.currentTimeMillis()
        }
    }

    enum class StatType {
        CONSUMED_BOTTLES_BY_COLORS,
        CONSUMED_BOTTLES_BY_VINTAGE,
    }
}
