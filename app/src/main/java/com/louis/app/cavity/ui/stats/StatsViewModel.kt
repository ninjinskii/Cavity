package com.louis.app.cavity.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.ui.stats.StatsUiModel.Pie.PieSlice
import com.louis.app.cavity.util.ColorUtil
import com.louis.app.cavity.util.DateFormatter
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val year = MutableLiveData(DateFormatter.roundToYear(System.currentTimeMillis()))

    val consumedBottlesByColor = year.switchMap { getConsumedBottlesByColor(it) }

    val consumedBottlesByVintage = year.switchMap { getConsumedBottlesByVintage(it) }

    private fun getConsumedBottlesByColor(year: Long) = liveData(IO) {
        // TODO: consider adding a History - BottleAndWine relation instead of BOundedEntry
        // TODO: consider merging year source and entries source and use switchMap to vaoir requesting bounded entries every time
        val entries = repository.getBoundedEntriesNotPagedNotLive()

        withContext(Default) {
            val max: Float
            val grouped = entries
                .filter { it.historyEntry.date > year && it.historyEntry.type == 0 }
                .also { max = it.size.toFloat() }
                .groupBy { it.bottleAndWine.wine.color }

            val consumedBottlesByColor = grouped.keys.map {
                PieSlice(
                    name = ColorUtil.getStringResForWineColor(it),
                    angle = (grouped[it]!!.size / max) * 360f,
                    color = ColorUtil.getColorResForWineColor(it)
                )
            }

            emit(consumedBottlesByColor)
        }
    }

    private fun getConsumedBottlesByVintage(year: Long) = liveData(IO) {
        val entries = repository.getBoundedEntriesNotPagedNotLive()

        withContext(Default) {
            val max: Float
            val grouped = entries
                .filter { it.historyEntry.date > year && it.historyEntry.type == 0 }
                .also { max = it.size.toFloat() }
                .sortedBy { it.bottleAndWine.bottle.vintage }
                .groupBy { it.bottleAndWine.bottle.vintage }

            val consumedBottlesByVintage = grouped.keys.map {
                PieSlice(
                    name = it,
                    angle = (grouped[it]!!.size / max) * 360f,
                    color = null
                )
            }

            emit(consumedBottlesByVintage)
        }
    }

    fun setYear(timestamp: Long) {
        val yearStart = DateFormatter.roundToYear(timestamp)
        year.value = yearStart
    }
}
