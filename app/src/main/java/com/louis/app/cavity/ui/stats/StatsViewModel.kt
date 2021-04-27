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

    private val _currentItemPosition = MutableLiveData<Int>()
    val currentItemPosition: LiveData<Int>
        get() = _currentItemPosition

    private val _showYearPicker = MutableLiveData<Boolean>()
    val showYearPicker: LiveData<Boolean>
        get() = _showYearPicker

//    fun getCountyStats() = statRequest.switchMap {
//        val start = it.year?.yearStart ?: 0
//        val end = it.year?.yearEnd ?: 0
//
//        when (it.statType) {
//            StatType.STOCK -> repository.getStockByCounty()
//            StatType.REPLENISHMENTS -> repository.getReplenishmentsByCounty(start, end)
//            StatType.CONSUMPTIONS -> repository.getConsumptionsByCounty(start, end)
//        }
//    }
//
//    fun getColorStats() = statRequest.switchMap {
//        val start = it.year?.yearStart ?: 0
//        val end = it.year?.yearEnd ?: 0
//
//        when (it.statType) {
//            StatType.STOCK -> repository.getStockByColor()
//            StatType.REPLENISHMENTS -> repository.getReplenishmentsByColor(start, end)
//            StatType.CONSUMPTIONS -> repository.getConsumptionsByColor(start, end)
//        }
//    }

//    fun getVintageStats() = statRequest.switchMap {
//
//    }

    fun results(globalStatType: StatGlobalType) = statRequest.switchMap {
        val start = it.year?.yearStart ?: 0
        val end = it.year?.yearEnd ?: 0

        when (globalStatType) {
            StatGlobalType.COUNTY -> when (it.statType) {
                StatType.STOCK -> repository.getStockByCounty()
                StatType.REPLENISHMENTS -> repository.getReplenishmentsByCounty(
                    start,
                    end
                )
                StatType.CONSUMPTIONS -> repository.getConsumptionsByCounty(
                    start,
                    end
                )
            }
            StatGlobalType.COLOR -> when (it.statType) {
                StatType.STOCK -> repository.getStockByColor()
                StatType.REPLENISHMENTS -> repository.getReplenishmentsByColor(
                    start,
                    end
                )
                StatType.CONSUMPTIONS -> repository.getConsumptionsByColor(
                    start,
                    end
                )
            }
            StatGlobalType.VINTAGE -> when (it.statType) {
                StatType.STOCK -> repository.getStockByVintage()
                StatType.REPLENISHMENTS -> repository.getReplenishmentsByVintage(
                    start,
                    end
                )
                StatType.CONSUMPTIONS -> repository.getConsumptionsByVintage(
                    start,
                    end
                )
            }
            StatGlobalType.NAMING -> when (it.statType) {
                StatType.STOCK -> repository.getStockByNaming()
                StatType.REPLENISHMENTS -> repository.getReplenishmentsByNaming(
                    start,
                    end
                )
                StatType.CONSUMPTIONS -> repository.getConsumptionsByNaming(
                    start,
                    end
                )
            }
        }
    }

    fun notifyPageChanged(position: Int) {
        _currentItemPosition.value = position
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
        _showYearPicker.value = statType != StatType.STOCK
    }

    fun setShouldShowYearPicker(show: Boolean) {
        _showYearPicker.value = show
    }
}

data class StatRequest(val statType: StatType, val year: Year?)

enum class StatType {
    STOCK,
    REPLENISHMENTS,
    CONSUMPTIONS,
}


