package com.louis.app.cavity.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.BoundedBottle
import com.louis.app.cavity.model.*
import com.louis.app.cavity.ui.search.filters.*
import com.louis.app.cavity.util.combineAsync
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch

class SearchViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)
    private val globalFilter = MutableLiveData<WineFilter>(FilterConsumed(false))

    val results: LiveData<List<BoundedBottle>> = repository
        .getBoundedBottles()
        .combineAsync(globalFilter) { receiver, bottles, filter ->
            filter(receiver, bottles, filter)
        }

    private var countyFilter: WineFilter = NoFilter
    private var colorFilter: WineFilter = NoFilter
    private var otherFilter: WineFilter = NoFilter
    private var vintageFilter: WineFilter = NoFilter
    private var textFilter: WineFilter = NoFilter
    private var priceFilter: WineFilter = NoFilter
    private var dateFilter: WineFilter = NoFilter
    private var grapeFilter: WineFilter = NoFilter
    private var reviewFilter: WineFilter = NoFilter
    private var selectedFilter: WineFilter = NoFilter
    private var consumedFilter: WineFilter = NoFilter
    private var capacityFilter: WineFilter = NoFilter

    var selectedCounties = emptyList<County>()
        private set

    var selectedGrapes = emptyList<Grape>()
        private set

    var selectedReviews = emptyList<Review>()
        private set

    var currentBeyondDate: Long? = null
        private set

    var currentUntilDate: Long? = null
        private set

    fun getAllCounties() = repository.getAllCounties()

    fun getAllGrapes() = repository.getAllGrapes()

    fun getAllReviews() = repository.getAllReviews()

    fun setCountiesFilters(filteredCounties: List<County>) {
        selectedCounties = filteredCounties

        val countyFilters: List<WineFilter> = selectedCounties.map { FilterCounty(it) }

        countyFilter =
            if (countyFilters.isNotEmpty())
                countyFilters.reduce { acc, filterCounty -> acc.orCombine(filterCounty) }
            else NoFilter

        updateFilters()
    }

    fun setColorFilters(colorCheckedChipIds: List<Int>) {
        val colorFilters = mutableListOf<WineFilter>()

        if (R.id.chipRed in colorCheckedChipIds)
            colorFilters.add(FilterColor(WineColor.RED))
        if (R.id.chipWhite in colorCheckedChipIds)
            colorFilters.add(FilterColor(WineColor.WHITE))
        if (R.id.chipSweet in colorCheckedChipIds)
            colorFilters.add(FilterColor(WineColor.SWEET))
        if (R.id.chipRose in colorCheckedChipIds)
            colorFilters.add(FilterColor(WineColor.ROSE))

        colorFilter =
            if (colorFilters.isNotEmpty())
                colorFilters.reduce { acc, wineFilter -> acc.orCombine(wineFilter) }
            else NoFilter

        updateFilters()
    }

    fun setOtherFilters(otherCheckedChipIds: List<Int>) {
        val otherFilters = mutableListOf<WineFilter>()

        if (R.id.chipReadyToDrink in otherCheckedChipIds) otherFilters.add(FilterReadyToDrink())
        if (R.id.chipOrganic in otherCheckedChipIds) otherFilters.add(FilterOrganic())
        if (R.id.chipFavorite in otherCheckedChipIds) otherFilters.add(FilterFavorite())
        if (R.id.chipPdf in otherCheckedChipIds) otherFilters.add(FilterPdf())

        // Consumed filter nneds a special treatment
        consumedFilter = FilterConsumed(R.id.chipConsume in otherCheckedChipIds)

        otherFilter =
            if (otherFilters.isNotEmpty())
                otherFilters.reduce { acc, wineFilter -> acc.andCombine(wineFilter) }
            else NoFilter

        updateFilters()
    }

    fun setVintageFilter(minValue: Int, maxValue: Int) {
        vintageFilter = FilterVintage(minValue, maxValue)
        updateFilters()
    }

    fun setTextFilter(query: String) {
        textFilter = if (query.isNotEmpty()) FilterText(query) else NoFilter
        updateFilters()
    }

    fun setPriceFilter(minValue: Int, maxValue: Int) {
        priceFilter = if (minValue != -1) FilterPrice(minValue, maxValue) else NoFilter
        updateFilters()
    }

    fun setBeyondFilter(beyond: Long?) {
        currentBeyondDate = beyond

        dateFilter =
            if (beyond == null && currentUntilDate == null)
                NoFilter
            else
                FilterDate(beyond, currentUntilDate)

        updateFilters()
    }

    fun setUntilFilter(until: Long?) {
        currentUntilDate = until

        dateFilter =
            if (until == null && currentBeyondDate == null)
                NoFilter
            else
                FilterDate(currentBeyondDate, until)

        updateFilters()
    }

    fun setGrapeFilters(filteredGrapes: List<Grape>) {
        selectedGrapes = filteredGrapes

        val grapeFilters: List<WineFilter> = selectedGrapes.map { FilterGrape(it) }

        grapeFilter =
            if (grapeFilters.isNotEmpty())
                grapeFilters.reduce { acc, filterGrape -> acc.orCombine(filterGrape) }
            else NoFilter

        updateFilters()
    }

    fun setReviewFilters(filteredReviews: List<Review>) {
        selectedReviews = filteredReviews

        val reviewFilters: List<WineFilter> = selectedReviews.map { FilterReview(it) }

        reviewFilter =
            if (reviewFilters.isNotEmpty())
                reviewFilters.reduce { acc, filterReview -> acc.orCombine(filterReview) }
            else NoFilter

        updateFilters()
    }

    fun setSelectedFilter(filter: Boolean) {
        reviewFilter = if (filter) FilterSelected() else NoFilter
        updateFilters()
    }

    fun setCapacityFilter(checkedButtonId: Int) {
        capacityFilter = when (checkedButtonId) {
            R.id.rbSlim -> FilterCapacity(BottleSize.SLIM)
            R.id.rbNormal -> FilterCapacity(BottleSize.NORMAL)
            R.id.rbMagnum -> FilterCapacity(BottleSize.MAGNUM)
            else /* View.NO_ID */ -> NoFilter
        }

        updateFilters()
    }

    private fun updateFilters() {
        if (consumedFilter is NoFilter) {
            consumedFilter = FilterConsumed(false)
        }

        val filters = listOf(
            countyFilter, colorFilter, otherFilter, vintageFilter, textFilter, priceFilter,
            dateFilter, grapeFilter, reviewFilter, selectedFilter, capacityFilter, consumedFilter
        )

        val combinedFilters = filters.reduce { acc, wineFilter -> acc.andCombine(wineFilter) }
        globalFilter.value = combinedFilters
    }

    private fun filter(
        receiver: MutableLiveData<List<BoundedBottle>>,
        bottles: List<BoundedBottle>,
        filter: WineFilter
    ) {
        viewModelScope.launch(Default) {
            val filtered = filter.meetFilters(bottles)
            receiver.postValue(filtered)
        }
    }
}

