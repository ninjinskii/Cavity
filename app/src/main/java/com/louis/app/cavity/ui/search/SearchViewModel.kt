package com.louis.app.cavity.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.ui.search.filters.NoFilter
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.relation.BottleAndWine
import com.louis.app.cavity.ui.home.WineColor
import com.louis.app.cavity.ui.search.filters.*
import com.louis.app.cavity.util.L
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class SearchViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)
    private val bottlesAndWine = mutableListOf<BottleAndWine>()

    // These filters are located in FragmentSearch
    private var countyFilter: WineFilter = NoFilter
    private var colorFilter: WineFilter = NoFilter
    private var otherFilter: WineFilter = NoFilter
    private var vintageFilter: WineFilter = NoFilter
    private var textFilter: WineFilter = NoFilter

    // These filters are located in FragmentMoreFilters
    private var priceFilter: WineFilter = NoFilter
    private var dateFilter: WineFilter = NoFilter
    private var stockFilter: WineFilter = NoFilter

    private val _results = MutableLiveData<List<BottleAndWine>>()
    val results: LiveData<List<BottleAndWine>>
        get() = _results

    var state = SearchState()
        private set

    init {
        viewModelScope.launch(IO) {
            bottlesAndWine.addAll(repository.getBottlesAndWineNotLive())
            _results.postValue(bottlesAndWine)
        }
    }

    suspend fun getAllCountiesNotLive() = repository.getAllCountiesNotLive()

    private fun filter() {
        viewModelScope.launch(Default) {
            val filters = listOf(
                countyFilter, colorFilter, otherFilter, vintageFilter,
                textFilter, priceFilter, dateFilter, stockFilter
            )

            val combinedFilters = filters.reduce { acc, wineFilter -> acc.andCombine(wineFilter) }

            val filtered = combinedFilters.meetFilters(bottlesAndWine)
            // Deleting 'toList()' seems to introduce a bug sometimes, where the observer is not
            // aware that the data has been changed, the first time you access FragmentSearch
            _results.postValue(filtered.toList())
        }
    }

    fun setCountiesFilters(filteredCounties: List<County>) {
        state.counties = filteredCounties.map { it.countyId }

        val countyFilters: List<WineFilter> = filteredCounties.map { FilterCounty(it.countyId) }

        countyFilter =
            if (countyFilters.isNotEmpty())
                countyFilters.reduce { acc, filterCounty -> acc.orCombine(filterCounty) }
            else NoFilter

        filter()
    }

    fun setColorFilters(colorCheckedChipIds: List<Int>) {
        state.colors = colorCheckedChipIds

        val colorFilters = mutableListOf<WineFilter>()

        if (R.id.chipRed in colorCheckedChipIds)
            colorFilters.add(FilterColor(WineColor.COLOR_RED))
        if (R.id.chipWhite in colorCheckedChipIds)
            colorFilters.add(FilterColor(WineColor.COLOR_WHITE))
        if (R.id.chipSweet in colorCheckedChipIds)
            colorFilters.add(FilterColor(WineColor.COLOR_SWEET))
        if (R.id.chipRose in colorCheckedChipIds)
            colorFilters.add(FilterColor(WineColor.COLOR_ROSE))

        colorFilter =
            if (colorFilters.isNotEmpty())
                colorFilters.reduce { acc, wineFilter -> acc.orCombine(wineFilter) }
            else NoFilter

        filter()
    }

    fun setOtherFilters(otherCheckedChipIds: List<Int>) {
        state.others = otherCheckedChipIds

        val otherFilters = mutableListOf<WineFilter>()

        if (R.id.chipReadyToDrink in otherCheckedChipIds) otherFilters.add(FilterReadyToDrink())
        if (R.id.chipOrganic in otherCheckedChipIds) otherFilters.add(FilterOrganic())
        if (R.id.chipFavorite in otherCheckedChipIds) otherFilters.add(FilterFavorite())
        if (R.id.chipPdf in otherCheckedChipIds) otherFilters.add(FilterPdf())

        otherFilter =
            if (otherFilters.isNotEmpty())
                otherFilters.reduce { acc, wineFilter -> acc.andCombine(wineFilter) }
            else NoFilter

        filter()
    }

    fun setVintageFilter(minValue: Int, maxValue: Int) {
        state.vintage = minValue to maxValue
        vintageFilter = FilterVintage(minValue, maxValue)
        filter()
    }

    fun setTextFilter(query: String) {
        textFilter = if (query.isNotEmpty()) FilterText(query) else NoFilter
        filter()
    }

    fun setPriceFilter(minValue: Int, maxValue: Int) {
        state.price = if (minValue != -1) minValue to maxValue else null
        priceFilter = if (minValue != -1) FilterPrice(minValue, maxValue) else NoFilter
        filter()
    }

    fun setBeyondFilter(beyond: Long?) {
        val currentUntilDate = state.date?.second
        state.date = beyond to currentUntilDate
        dateFilter =
            if (beyond == null && currentUntilDate == null)
                NoFilter
            else
                FilterDate(beyond, currentUntilDate)

        filter()
    }

    fun setUntilFilter(until: Long?) {
        val currentBeyondDate = state.date?.first
        state.date = currentBeyondDate to until
        dateFilter =
            if (until == null && currentBeyondDate == null)
                NoFilter
            else
                FilterDate(currentBeyondDate, until)

        filter()
    }

    fun setStockFilter(minValue: Int, maxValue: Int) {
        state.stock = minValue to maxValue
        stockFilter = FilterStock(minValue, maxValue)
        filter()
    }

    fun reset() {
        countyFilter = NoFilter
        colorFilter = NoFilter
        otherFilter = NoFilter
        vintageFilter = NoFilter
        textFilter = NoFilter
        priceFilter = NoFilter
        dateFilter = NoFilter
        stockFilter = NoFilter

        state = SearchState()

        filter()
    }

    override fun onCleared() {
        L.v("search viewModel cleared")
        super.onCleared()
    }

    data class SearchState(
        var counties: List<Long>? = null,
        var colors: List<Int>? = null,
        var others: List<Int>? = null,
        var vintage: Pair<Int, Int>? = null,
        var price: Pair<Int, Int>? = null,
        var date: Pair<Long?, Long?>? = null,
        var stock: Pair<Int, Int>? = null
    )
}
