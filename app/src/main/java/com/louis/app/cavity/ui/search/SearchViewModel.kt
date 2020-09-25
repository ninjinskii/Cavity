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
import com.louis.app.cavity.util.L
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
        val otherFilters = prepareOtherFilter(checkedChipIds)

        viewModelScope.launch(IO) {
            val winesWithBottles = repository.getWineWithBottlesNotLive()
            var filterCounty: WineFilter = NoFilter()
            var filterOther: WineFilter = NoFilter()

            filteredCounties.forEach {
                filterCounty = filterCounty add FilterCounty(it.countyId)
            }

            otherFilters.forEach { filterOther = filterOther and it }

            _results.postValue(And(filterCounty, filterOther).meetFilters(winesWithBottles))
        }

    }

    private fun prepareOtherFilter(checkedChipIds: List<Int>): List<WineFilter> {
        val filters = mutableListOf<WineFilter>()

        if (R.id.chipReadyToDrink in checkedChipIds) filters.add(FilterReadyToDrink())
        if (R.id.chipRed in checkedChipIds) filters.add(FilterRed())
        if (R.id.chipWhite in checkedChipIds) filters.add(FilterWhite())
        if (R.id.chipSweet in checkedChipIds) filters.add(FilterSweet())
        if (R.id.chipRose in checkedChipIds) filters.add(FilterRose())
        if (R.id.chipOrganic in checkedChipIds) filters.add(FilterOrganic())

        return filters
    }

    private infix fun WineFilter.add(filter: WineFilter): WineFilter {
        return if (this is NoFilter) {
            L.v("addedFilter : ${filter.javaClass.name}", "FILTER")
            filter
        } else {
            L.v("addedFilter : ${filter.javaClass.name}", "FILTER")
            Or(this, filter)
        }
    }

    private infix fun WineFilter.and(filter: WineFilter): WineFilter {
        return if (this is NoFilter) {
            L.v("addedFilter : ${filter.javaClass.name}", "FILTER")
            filter
        } else {
            L.v("addedFilter : ${filter.javaClass.name}", "FILTER")
            And(this, filter)
        }
    }

    data class FilterConstraint(
        val isReadyToDrink: FilterReadyToDrink? = null,
        val isRed: FilterRed? = null,
        val isWhite: FilterWhite? = null,
        val isSweet: FilterSweet? = null,
        val isRose: FilterRose? = null,
        val isOrganic: FilterOrganic? = null
    )
}
