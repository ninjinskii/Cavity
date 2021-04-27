package com.louis.app.cavity.ui.stats

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.Stat
import com.louis.app.cavity.db.dao.Year

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val groupedYears = Year("Combiner", 0L, System.currentTimeMillis())
    private val year = MutableLiveData(groupedYears)
    private val comparisonYear = MutableLiveData(groupedYears)

    val years: LiveData<List<Year>> = repository.getYears().map {
        it.toMutableList().apply {
            add(0, groupedYears)
            add(groupedYears)
        }
    }

    private val _currentItemPosition = MutableLiveData<Int>()
    val currentItemPosition: LiveData<Int>
        get() = _currentItemPosition

    private val _showYearPicker = MutableLiveData(false)
    val showYearPicker: LiveData<Boolean>
        get() = _showYearPicker

    private val _comparison = MutableLiveData(false)
    val comparison: LiveData<Boolean>
        get() = _comparison

    val comparisonStats: LiveData<List<Stat>> = comparisonYear.switchMap {
        MediatorLiveData<List<Stat>>().apply {
            addSource(repository.getConsumptionsByCounty(it.yearStart, it.yearEnd)) { value = it }
            addSource(repository.getReplenishmentsByCounty(it.yearStart, it.yearEnd)) { value = it }
            addSource(repository.getConsumptionsByColor(it.yearStart, it.yearEnd)) { value = it }
            addSource(repository.getReplenishmentsByColor(it.yearStart, it.yearEnd)) { value = it }
            addSource(repository.getConsumptionsByVintage(it.yearStart, it.yearEnd)) { value = it }
            addSource(repository.getReplenishmentsByVintage(it.yearStart, it.yearEnd)) {
                value = it
            }
            addSource(repository.getConsumptionsByNaming(it.yearStart, it.yearEnd)) { value = it }
            addSource(repository.getReplenishmentsByNaming(it.yearStart, it.yearEnd)) { value = it }
        }
    }

    fun getCountyStats(statType: StatType) = year.switchMap {
        val start = it?.yearStart ?: 0
        val end = it?.yearEnd ?: System.currentTimeMillis()

        when (statType) {
            StatType.STOCK -> repository.getStockByCounty() // post null to comparison
            StatType.REPLENISHMENTS -> repository.getReplenishmentsByCounty(start, end)
            StatType.CONSUMPTIONS -> repository.getConsumptionsByCounty(start, end)
        }
    }

    fun getColorStats(statType: StatType) = year.switchMap {
        val start = it?.yearStart ?: 0
        val end = it?.yearEnd ?: System.currentTimeMillis()

        when (statType) {
            StatType.STOCK -> repository.getStockByColor()
            StatType.REPLENISHMENTS -> repository.getReplenishmentsByColor(start, end)
            StatType.CONSUMPTIONS -> repository.getConsumptionsByColor(start, end)
        }
    }

    fun getVintageStats(statType: StatType) = year.switchMap {
        val start = it?.yearStart ?: 0
        val end = it?.yearEnd ?: System.currentTimeMillis()

        when (statType) {
            StatType.STOCK -> repository.getStockByVintage()
            StatType.REPLENISHMENTS -> repository.getConsumptionsByVintage(start, end)
            StatType.CONSUMPTIONS -> repository.getConsumptionsByVintage(start, end)
        }
    }

    fun getNamingStats(statType: StatType) = year.switchMap {
        val start = it?.yearStart ?: 0
        val end = it?.yearEnd ?: System.currentTimeMillis()

        when (statType) {
            StatType.STOCK -> repository.getStockByNaming()
            StatType.REPLENISHMENTS -> repository.getConsumptionsByNaming(start, end)
            StatType.CONSUMPTIONS -> repository.getConsumptionsByNaming(start, end)
        }
    }

    fun notifyPageChanged(position: Int) {
        _currentItemPosition.value = position
    }

    fun setYear(year: Year) {
        val currentYear = this.year.value!!

        if (year != currentYear) {
            this.year.value = year
        }
    }

    fun setShouldShowYearPicker(show: Boolean) {
        val currentValue = _showYearPicker.value!!

        if (show != currentValue) {
            _showYearPicker.value = show
        }
    }

    fun toggleComparison() {
        _comparison.value = !comparison.value!!
    }
}

enum class StatType {
    STOCK,
    REPLENISHMENTS,
    CONSUMPTIONS,
}


