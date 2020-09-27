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

    fun filter(filteredCounties: List<County>, checkedChipIds: List<Int>) {
        val filters = prepareFilters(filteredCounties, checkedChipIds)

        viewModelScope.launch(IO) {
            val winesWithBottles = repository.getWineWithBottlesNotLive()
            val combinedFilters = filters.first.andCombine(filters.second)
            _results.postValue(combinedFilters.meetFilters(winesWithBottles))
        }
    }

    private fun prepareFilters(
        filteredCounties: List<County>,
        checkedChipIds: List<Int>
    ): Pair<WineFilter, WineFilter> {
        val otherFilters = mutableListOf<WineFilter>()
        val countiesFilters: List<WineFilter> = filteredCounties.map { FilterCounty(it.countyId) }

        if (R.id.chipReadyToDrink in checkedChipIds) otherFilters.add(FilterReadyToDrink())
        if (R.id.chipRed in checkedChipIds) otherFilters.add(FilterRed())
        if (R.id.chipWhite in checkedChipIds) otherFilters.add(FilterWhite())
        if (R.id.chipSweet in checkedChipIds) otherFilters.add(FilterSweet())
        if (R.id.chipRose in checkedChipIds) otherFilters.add(FilterRose())
        if (R.id.chipOrganic in checkedChipIds) otherFilters.add(FilterOrganic())

        val combinedCounty = if (countiesFilters.isNotEmpty())
            countiesFilters.reduce { acc, filterCounty -> acc.orCombine(filterCounty) }
        else NoFilter()

        val combinedOther =
            if (otherFilters.isNotEmpty())
                otherFilters.reduce { acc, wineFilter -> acc.andCombine(wineFilter) }
            else NoFilter()

        return combinedCounty to combinedOther
    }
}
