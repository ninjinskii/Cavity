package com.louis.app.cavity.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.relation.BottleAndWine
import com.louis.app.cavity.ui.home.WineColor
import com.louis.app.cavity.ui.search.filters.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class SearchViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)
    private val bottlesAndWine = mutableListOf<BottleAndWine>()

    // These filters are located in FragmentSearch
    private var countyFilter: WineFilter by Delegates.observable(NoFilter()) { _, _, _ -> filter() }
    private var colorFilter: WineFilter by Delegates.observable(NoFilter()) { _, _, _ -> filter() }
    private var otherFilter: WineFilter by Delegates.observable(NoFilter()) { _, _, _ -> filter() }
    private var vintageFilter: WineFilter by Delegates.observable(NoFilter()) { _, _, _ -> filter() }
    private var textFilter: WineFilter by Delegates.observable(NoFilter()) { _, _, _ -> filter() }

    // These filters are located in FragmentMoreFilters
    private var priceFilter: WineFilter by Delegates.observable(NoFilter()) { _, _, _ -> filter() }
    private var dateFilter: WineFilter by Delegates.observable(NoFilter()) { _, _, _ -> filter() }
    private var stockFilter: WineFilter by Delegates.observable(NoFilter()) { _, _, _ -> filter() }

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

    private fun filter() {
        viewModelScope.launch(Default) {
            val combinedFilters = countyFilter
                .andCombine(colorFilter)
                .andCombine(otherFilter)
                .andCombine(vintageFilter)
                .andCombine(textFilter)
                .andCombine(priceFilter)
                .andCombine(dateFilter)
                .andCombine(stockFilter)

            val filtered = combinedFilters.meetFilters(bottlesAndWine)
            _results.postValue(filtered)
        }
    }

    fun setCountiesFilters(filteredCounties: List<County>) {
        val countyFilters: List<WineFilter> = filteredCounties.map { FilterCounty(it.countyId) }

        countyFilter =
            if (countyFilters.isNotEmpty())
                countyFilters.reduce { acc, filterCounty -> acc.orCombine(filterCounty) }
            else NoFilter()
    }

    fun setColorFilters(colorCheckedChipIds: List<Int>) {
        val colorFilters = mutableListOf<WineFilter>()

        if (R.id.chipRed in colorCheckedChipIds)
            colorFilters.add(ColorFilter(WineColor.COLOR_RED))
        if (R.id.chipWhite in colorCheckedChipIds)
            colorFilters.add(ColorFilter(WineColor.COLOR_WHITE))
        if (R.id.chipSweet in colorCheckedChipIds)
            colorFilters.add(ColorFilter(WineColor.COLOR_SWEET))
        if (R.id.chipRose in colorCheckedChipIds)
            colorFilters.add(ColorFilter(WineColor.COLOR_ROSE))

        colorFilter =
            if (colorFilters.isNotEmpty())
                colorFilters.reduce { acc, wineFilter -> acc.orCombine(wineFilter) }
            else NoFilter()
    }

    fun setOtherFilters(otherCheckedChipIds: List<Int>) {
        val otherFilters = mutableListOf<WineFilter>()

        if (R.id.chipReadyToDrink in otherCheckedChipIds) otherFilters.add(FilterReadyToDrink())
        if (R.id.chipOrganic in otherCheckedChipIds) otherFilters.add(FilterOrganic())
        if (R.id.chipFavorite in otherCheckedChipIds) otherFilters.add(FilterFavorite())
        if (R.id.chipPdf in otherCheckedChipIds) otherFilters.add(FilterPdf())

        otherFilter =
            if (otherFilters.isNotEmpty())
                otherFilters.reduce { acc, wineFilter -> acc.andCombine(wineFilter) }
            else NoFilter()
    }

    fun setVintageFilter(minValue: Int, maxValue: Int) {
        vintageFilter = FilterVintage(minValue, maxValue)
    }

    fun setTextFilter(query: String) {
        textFilter = if (query.isNotEmpty()) FilterText(query) else NoFilter()
    }

    fun setPriceFilter(minValue: Int, maxValue: Int) {
        priceFilter = if (minValue == -1) NoFilter() else FilterPrice(minValue, maxValue)
    }

    fun setDateFilter(date: Long, searchBefore: Boolean) {
        dateFilter = if (date == -1L) NoFilter() else FilterDate(date, searchBefore)
    }

    fun setStockFilter(minValue: Int, maxValue: Int) {
        stockFilter = FilterStock(minValue, maxValue)
    }
}
