package com.louis.app.cavity.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.Year

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val statRequest =
        MutableLiveData(StatRequest(StatType.STOCK, Year("null", 0L, System.currentTimeMillis())))

    val years = repository.getYears()

    fun <T> results(globalStatType: StatGlobalType) = statRequest.switchMap {
        val start = it.year?.yearStart ?: 0
        val end = it.year?.yearEnd ?: 0

        when (globalStatType) {
            StatGlobalType.COLOR -> when (it.statType) {
                StatType.STOCK -> repository.getStockByColor() as LiveData<List<T>>
                StatType.REPLENISHMENTS -> repository.getReplenishmentsByColor(
                    start,
                    end
                ) as LiveData<List<T>>
                StatType.CONSUMPTIONS -> repository.getConsumptionsByColor(
                    start,
                    end
                ) as LiveData<List<T>>
            }
            else -> when (it.statType) {
                StatType.STOCK -> repository.getStockByVintage() as LiveData<List<T>>
                StatType.REPLENISHMENTS -> repository.getReplenishmentsByVintage(
                    start,
                    end
                ) as LiveData<List<T>>
                StatType.CONSUMPTIONS -> repository.getConsumptionsByVintage(
                    start,
                    end
                ) as LiveData<List<T>>
            }
        }
    }

    fun setYear(year: Year) {
        val currentYear = statRequest.value?.year ?: Year("null", 0L, System.currentTimeMillis())
        val statType = statRequest.value?.statType ?: StatType.STOCK

        if (currentYear == year) return

        statRequest.value = StatRequest(statType, year)
    }

    fun setStatType(statType: StatType) {
        val year = statRequest.value?.year ?: Year("null", 0L, System.currentTimeMillis())
        statRequest.value = StatRequest(statType, year)
    }
}

data class StatRequest(val statType: StatType, val year: Year?)

enum class StatType {
    STOCK,
    REPLENISHMENTS,
    CONSUMPTIONS,
}


