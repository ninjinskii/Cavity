package com.louis.app.cavity.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.CavityDatabase
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.relation.BottleAndWine
import com.louis.app.cavity.ui.search.filters.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository(CavityDatabase.getInstance(app))
    private val bottlesAndWine = mutableListOf<BottleAndWine>()

    private val _results = MutableLiveData<List<BottleAndWine>>()
    val results: LiveData<List<BottleAndWine>>
        get() = _results

    init {
        viewModelScope.launch(IO) {
            bottlesAndWine.addAll(repository.getBottlesAndWineNotLive())
            _results.postValue(bottlesAndWine)
        }
    }

    fun getAllCountiesNotLive() = repository.getAllCountiesNotLive()

    fun filter(
        filteredCounties: List<County>,
        colorCheckedChipIds: List<Int>,
        otherCheckedChipIds: List<Int>,
        filteredDate: Pair<Long, Boolean>,
        filteredQuery: String
    ) {
        val filters = prepareChipFilters(filteredCounties, colorCheckedChipIds, otherCheckedChipIds)
        val dateFilter =
            if (filteredDate.first != -1L)
                FilterDate(filteredDate.first, filteredDate.second)
            else NoFilter()
        val queryFilter =
            if (filteredQuery.isNotEmpty())
                FilterText(filteredQuery)
            else NoFilter()


        viewModelScope.launch(IO) {
            val bottlesAndWine = repository.getBottlesAndWineNotLive()

            withContext(Default) {
                val combinedFilters = filters.first
                    .andCombine(filters.second)
                    .andCombine(filters.third)
                    .andCombine(dateFilter)
                    .andCombine(queryFilter)

                val filtered = combinedFilters.meetFilters(bottlesAndWine)
                _results.postValue(filtered)
            }
        }
    }

    private fun prepareChipFilters(
        filteredCounties: List<County>,
        colorCheckedChipIds: List<Int>,
        otherCheckedChipIds: List<Int>
    ): Triple<WineFilter, WineFilter, WineFilter> {
        val colorFilters = mutableListOf<WineFilter>()
        val otherFilters = mutableListOf<WineFilter>()
        val countiesFilters: List<WineFilter> = filteredCounties.map { FilterCounty(it.countyId) }

        if (R.id.chipReadyToDrink in otherCheckedChipIds) otherFilters.add(FilterReadyToDrink())
        if (R.id.chipOrganic in otherCheckedChipIds) otherFilters.add(FilterOrganic())

        if (R.id.chipRed in colorCheckedChipIds) colorFilters.add(FilterRed())
        if (R.id.chipWhite in colorCheckedChipIds) colorFilters.add(FilterWhite())
        if (R.id.chipSweet in colorCheckedChipIds) colorFilters.add(FilterSweet())
        if (R.id.chipRose in colorCheckedChipIds) colorFilters.add(FilterRose())

        val combinedCounty =
            if (countiesFilters.isNotEmpty())
                countiesFilters.reduce { acc, filterCounty -> acc.orCombine(filterCounty) }
            else NoFilter()

        val combinedColor =
            if (colorFilters.isNotEmpty())
                colorFilters.reduce { acc, wineFilter -> acc.orCombine(wineFilter) }
            else NoFilter()

        val combinedOther =
            if (otherFilters.isNotEmpty())
                otherFilters.reduce { acc, wineFilter -> acc.andCombine(wineFilter) }
            else NoFilter()

        return Triple(combinedCounty, combinedColor, combinedOther)
    }
}
