package com.louis.app.cavity.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.ui.stats.StatsUiModel.Pie.PieSlice
import com.louis.app.cavity.util.ColorUtil
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    fun getConsumedWinesByColor() = liveData(IO) {
        // TODO: consider adding a History - BottleAndWine relation instead of BOundedEntry
        val entries = repository.getBoundedEntriesNotPagedNotLive()

        withContext(Default) {
            val max: Float
            val grouped = entries
                .filter { it.historyEntry.type == 0 }
                .also { max = it.size.toFloat() }
                .groupBy { it.bottleAndWine.wine.color }

            val consumedWinesByColor = grouped.keys.map {
                PieSlice(
                    name = ColorUtil.getStringResForWineColor(it),
                    angle = (grouped[it]!!.size / max) * 360f,
                    color = ColorUtil.getColorResForWineColor(it)
                )
            }

            emit(consumedWinesByColor)
        }
    }
}
