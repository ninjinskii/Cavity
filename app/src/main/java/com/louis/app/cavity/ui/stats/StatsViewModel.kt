package com.louis.app.cavity.ui.stats

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.ui.stats.StatsUiModel.Pie.PieSlice
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    fun getConsumedWinesByColor() = liveData(IO) {
        // TODO: consider adding a History - BottleAndWine relation instead of BOundedEntry
        val entries = repository.getBoundedEntriesNotPagedNotLive()

        withContext(Default) {
            val grouped = entries
                .filter { it.historyEntry.type == 0 }
                .groupBy { it.bottleAndWine.wine.color }

            val max = entries.filter { it.historyEntry.type == 0 }.size.toFloat()
            val colors = listOf(Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA)

            val consumedWinesByColor = grouped.keys.mapIndexed { index, it ->
                PieSlice("Name", angle = (grouped[it]!!.size / max) * 360f, color = colors[index])
            }

            emit(consumedWinesByColor)
        }
    }
}
