package com.louis.app.cavity.ui.stats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.Stat
import com.louis.app.cavity.db.dao.Year

class LiveDataStatsFactory(
    private val repository: WineRepository,
    private val year: MutableLiveData<Year>,
    private val comparisonYear: MutableLiveData<Year>
) {

    private val statTypes = MutableList(4) {
        MutableLiveData(StatType.STOCK)
    }

    private val _results: MutableList<LiveData<List<Stat>>> = MutableList(4) {
        createLiveStat(it)
    }
    val results: List<LiveData<List<Stat>>>
        get() = _results

    fun addStat(): LiveData<List<Stat>> {
        statTypes.add(MutableLiveData(StatType.STOCK))

        return createLiveStat(_results.size).also {
            _results.add(it)
        }
    }

    fun applyStatType(position: Int, statType: StatType) {
        statTypes[position].value = statType
    }

    fun getLiveStatType(position: Int): LiveData<StatType> = statTypes[position]

    fun getStatType(position: Int): StatType = statTypes[position].value ?: StatType.STOCK

    private fun createLiveStat(position: Int) = year.switchMap { year ->
        statTypes[position].switchMap { statType ->
            getStat(position, year, statType)
        }
    }

    // It can't be anything else.
    @Suppress("UNCHECKED_CAST")
    private fun getStat(position: Int, year: Year, statType: StatType): LiveData<List<Stat>> {
        val start = year.yearStart
        val end = year.yearEnd

        return when (position) {
            0 -> when (statType) {
                StatType.STOCK -> repository.getStockByCounty()
                StatType.REPLENISHMENTS -> repository.getReplenishmentsByCounty(start, end)
                StatType.CONSUMPTIONS -> repository.getConsumptionsByCounty(start, end)
            } as LiveData<List<Stat>>
            1 -> when (statType) {
                StatType.STOCK -> repository.getStockByColor()
                StatType.REPLENISHMENTS -> repository.getReplenishmentsByColor(start, end)
                StatType.CONSUMPTIONS -> repository.getConsumptionsByColor(start, end)
            } as LiveData<List<Stat>>
            2 -> when (statType) {
                StatType.STOCK -> repository.getStockByVintage()
                StatType.REPLENISHMENTS -> repository.getReplenishmentsByVintage(start, end)
                StatType.CONSUMPTIONS -> repository.getConsumptionsByVintage(start, end)
            } as LiveData<List<Stat>>
            else -> when (statType) {
                StatType.STOCK -> repository.getStockByNaming()
                StatType.REPLENISHMENTS -> repository.getReplenishmentsByNaming(start, end)
                StatType.CONSUMPTIONS -> repository.getConsumptionsByNaming(start, end)
            } as LiveData<List<Stat>>
        }
    }
}
