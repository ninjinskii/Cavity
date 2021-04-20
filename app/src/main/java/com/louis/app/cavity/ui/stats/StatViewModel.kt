package com.louis.app.cavity.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.ui.stats.StatUiModel.Pie.PieSlice
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class StatViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    fun getConsumedWinesByColor() = liveData(IO) {
        val entries = repository.getBoundedEntriesNotPagedNotLive()

        withContext(Default) {
            val grouped = entries.groupBy { it.bottleAndWine.wine.color }
            val max = entries.size

            val consumedWinesByColor = grouped.keys.map {
                PieSlice("Name", angle = (grouped[it]!!.size / max) * 100, color = null)
            }

            emit(consumedWinesByColor)
        }
    }
}
