package com.louis.app.cavity.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.BoundedHistoryEntry
import com.louis.app.cavity.util.ColorUtil
import com.louis.app.cavity.util.DateFormatter
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val year = MutableLiveData(DateFormatter.getYearBounds(System.currentTimeMillis()))

    private val entries = year.switchMap {
        repository.getBoundedEntriesBetween(it.first, it.second)
    }

    val display = entries.switchMap { prepareResults(it) }

    private fun prepareResults(entries: List<BoundedHistoryEntry>) = liveData(Default) {
        val results = mutableListOf<StatsUiModel>()

        withContext(Default) {
            val consumedBottlesByColor = async { getConsumedBottlesByColor(entries) }
            val consumedBottlesByVintage = async { getConsumedBottlesByVintage(entries) }

            results.addAll(
                listOf(
                    StatsUiModel.Pie(consumedBottlesByColor.await()),
                    StatsUiModel.Pie(consumedBottlesByVintage.await())
                )
            )

            emit(results)
        }
    }

    private fun getConsumedBottlesByColor(
        entries: List<BoundedHistoryEntry>
    ): List<PieSlice> {

        val max: Float
        val grouped = entries
            .filter { it.historyEntry.type == 0 }
            .also { max = it.size.toFloat() }
            .sortedBy { it.bottleAndWine.wine.color }
            .groupBy { it.bottleAndWine.wine.color }

        return grouped.keys.map {
            ResPieSlice(
                name = ColorUtil.getStringResForWineColor(it),
                angle = (grouped[it]!!.size / max) * 360f,
                color = ColorUtil.getColorResForWineColor(it)
            )
        }
    }

    private fun getConsumedBottlesByVintage(
        entries: List<BoundedHistoryEntry>
    ): List<PieSlice> {

        val max: Float
        val grouped = entries
            .filter { it.historyEntry.type == 0 }
            .also { max = it.size.toFloat() }
            .sortedBy { it.bottleAndWine.bottle.vintage }
            .groupBy { it.bottleAndWine.bottle.vintage }

        return grouped.keys.map {
            StringPieSlice(
                name = it.toString(),
                angle = (grouped[it]!!.size / max) * 360f,
                color = null
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
}
