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
import com.louis.app.cavity.model.relation.WineWithBottles
import com.louis.app.cavity.ui.search.filters.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class SearchViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository(CavityDatabase.getInstance(app))

    private val _results = MutableLiveData<List<WineWithBottles>>()
    val results: LiveData<List<WineWithBottles>>
        get() = _results

    init {
        viewModelScope.launch(IO) {
            _results.postValue(repository.getWineWithBottlesNotLive())
        }
    }

    fun getAllCountiesNotLive() = repository.getAllCountiesNotLive()

    fun filter(
        filteredCounties: List<County>,
        colorCheckedChipIds: List<Int>,
        otherCheckedChipIds: List<Int>
    ) {
        val filters = prepareFilters(filteredCounties, colorCheckedChipIds, otherCheckedChipIds)

        viewModelScope.launch(IO) {
            val winesWithBottles = repository.getWineWithBottlesNotLive()
            val combinedFilters = filters.first.andCombine(filters.second).andCombine(filters.third)
            _results.postValue(combinedFilters.meetFilters(winesWithBottles))
        }
    }

    private fun prepareFilters(
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
