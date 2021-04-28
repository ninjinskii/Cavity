package com.louis.app.cavity.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.Year

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val groupedYears = Year("Combiner", 0L, System.currentTimeMillis())
    private val year = MutableLiveData(groupedYears)
    private val comparisonYear = MutableLiveData(groupedYears)

    private val statFactory = LiveDataStatsFactory(repository, year, comparisonYear)

    val years: LiveData<List<Year>> = repository.getYears().map {
        it.toMutableList().apply {
            add(0, groupedYears)
            add(groupedYears)
        }
    }

    val results = statFactory.results

    private val _currentItemPosition = MutableLiveData<Int>()
    val currentItemPosition: LiveData<Int>
        get() = _currentItemPosition

    private val _showYearPicker = MutableLiveData(false)
    val showYearPicker: LiveData<Boolean>
        get() = _showYearPicker

    private val _comparison = MutableLiveData(false)
    val comparison: LiveData<Boolean>
        get() = _comparison

    fun statType(viewPagerPos: Int) = statFactory.getLiveStatType(viewPagerPos)

    fun getStatType(viewPagerPos: Int) = statFactory.getStatType(viewPagerPos)

    fun setStatType(viewPagerPos: Int, statType: StatType) {
        statFactory.applyStatType(viewPagerPos, statType)
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


