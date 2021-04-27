package com.louis.app.cavity.ui.stats

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.Year

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val year = MutableLiveData(Year("null", 0L, System.currentTimeMillis()))

    val years: LiveData<List<Year>> = repository.getYears().map {
        it.toMutableList().apply {
            add(0, Year("Toutes années confondues", 0L, System.currentTimeMillis()))
            add(Year("Toutes années confondues", 0L, System.currentTimeMillis()))
        }
    }

    private val _currentItemPosition = MutableLiveData<Int>()
    val currentItemPosition: LiveData<Int>
        get() = _currentItemPosition

    private val _showYearPicker = MutableLiveData<Boolean>()
    val showYearPicker: LiveData<Boolean>
        get() = _showYearPicker

    fun getCountyStats(statType: StatType) = year.switchMap {
        val start = it?.yearStart ?: 0
        val end = it?.yearEnd ?: System.currentTimeMillis()

        setShouldShowYearPicker(statType != StatType.STOCK)

        when (statType) {
            StatType.STOCK -> repository.getStockByCounty()
            StatType.REPLENISHMENTS -> repository.getReplenishmentsByCounty(start, end)
            StatType.CONSUMPTIONS -> repository.getConsumptionsByCounty(start, end)
        }
    }

    fun getColorStats(statType: StatType) = year.switchMap {
        val start = it?.yearStart ?: 0
        val end = it?.yearEnd ?: System.currentTimeMillis()

        setShouldShowYearPicker(statType != StatType.STOCK)

        when (statType) {
            StatType.STOCK -> repository.getStockByColor()
            StatType.REPLENISHMENTS -> repository.getReplenishmentsByColor(start, end)
            StatType.CONSUMPTIONS -> repository.getConsumptionsByColor(start, end)
        }
    }

    fun getVintageStats(statType: StatType) = year.switchMap {
        val start = it?.yearStart ?: 0
        val end = it?.yearEnd ?: System.currentTimeMillis()

        setShouldShowYearPicker(statType != StatType.STOCK)

        when (statType) {
            StatType.STOCK -> repository.getStockByVintage()
            StatType.REPLENISHMENTS -> repository.getConsumptionsByVintage(start, end)
            StatType.CONSUMPTIONS -> repository.getConsumptionsByVintage(start, end)
        }
    }

    fun getNamingStats(statType: StatType) = year.switchMap {
        val start = it?.yearStart ?: 0
        val end = it?.yearEnd ?: System.currentTimeMillis()

        setShouldShowYearPicker(statType != StatType.STOCK)

        when (statType) {
            StatType.STOCK -> repository.getStockByNaming()
            StatType.REPLENISHMENTS -> repository.getConsumptionsByNaming(start, end)
            StatType.CONSUMPTIONS -> repository.getConsumptionsByNaming(start, end)
        }
    }

    fun results(globalStatType: StatGlobalType, statType: StatType) = year.switchMap {
        val start = it?.yearStart ?: 0
        val end = it?.yearEnd ?: System.currentTimeMillis()

        when (globalStatType) {
            StatGlobalType.COUNTY -> when (statType) {
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
            StatGlobalType.COLOR -> when (statType) {
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
            StatGlobalType.VINTAGE -> when (statType) {
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
            StatGlobalType.NAMING -> when (statType) {
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
        val currentYear = this.year.value ?: Year("null", 0L, System.currentTimeMillis())

        if (currentYear != year) {
            this.year.value = currentYear
        }
    }

    fun setShouldShowYearPicker(show: Boolean) {
        _showYearPicker.value = show
    }
}

enum class StatType {
    STOCK,
    REPLENISHMENTS,
    CONSUMPTIONS,
}


