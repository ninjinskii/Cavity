package com.louis.app.cavity.ui.search

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.BoundedBottle
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.ui.search.filters.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch

class SearchViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)
    private val dumbEvent = MutableLiveData<Unit>()

    private val _results = MediatorLiveData<List<BoundedBottle>>().apply {
        addSource(repository.getBoundedBottles()) {
            bottles = it
            filter(it)
        }
        addSource(dumbEvent) { filter(bottles) }
    }
    val results: LiveData<List<BoundedBottle>>
        get() = _results

    private var bottles = emptyList<BoundedBottle>()

    private var currentBeyondDate: Long? = null
    private var currentUntilDate: Long? = null

    private var countyFilter: WineFilter = NoFilter
    private var colorFilter: WineFilter = NoFilter
    private var otherFilter: WineFilter = NoFilter
    private var vintageFilter: WineFilter = NoFilter
    private var textFilter: WineFilter = NoFilter
    private var priceFilter: WineFilter = NoFilter
    private var dateFilter: WineFilter = NoFilter
    private var grapeFilter: WineFilter = NoFilter
    private var reviewFilter: WineFilter = NoFilter

    var selectedCounties = emptyList<County>()
        private set

    var selectedGrapes = emptyList<Grape>()
        private set

    var selectedReviews = emptyList<Review>()
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

        dumbEvent.postValue(Unit)
    }

    fun setColorFilters(colorCheckedChipIds: List<Int>) {
        val colorFilters = mutableListOf<WineFilter>()

        if (R.id.chipRed in colorCheckedChipIds)
            colorFilters.add(FilterColor(0))
        if (R.id.chipWhite in colorCheckedChipIds)
            colorFilters.add(FilterColor(1))
        if (R.id.chipSweet in colorCheckedChipIds)
            colorFilters.add(FilterColor(1))
        if (R.id.chipRose in colorCheckedChipIds)
            colorFilters.add(FilterColor(3))

        colorFilter =
            if (colorFilters.isNotEmpty())
                colorFilters.reduce { acc, wineFilter -> acc.orCombine(wineFilter) }
            else NoFilter

        dumbEvent.postValue(Unit)
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
            else NoFilter

        dumbEvent.postValue(Unit)
    }

    fun setVintageFilter(minValue: Int, maxValue: Int) {
        vintageFilter = FilterVintage(minValue, maxValue)
        dumbEvent.postValue(Unit)
    }

    fun setTextFilter(query: String) {
        textFilter = if (query.isNotEmpty()) FilterText(query) else NoFilter
        dumbEvent.postValue(Unit)
    }

    fun setPriceFilter(minValue: Int, maxValue: Int) {
        priceFilter = if (minValue != -1) FilterPrice(minValue, maxValue) else NoFilter
        dumbEvent.postValue(Unit)
    }

    fun setBeyondFilter(beyond: Long?) {
        currentBeyondDate = beyond

        dateFilter =
            if (beyond == null && currentUntilDate == null)
                NoFilter
            else
                FilterDate(beyond, currentUntilDate)

        dumbEvent.postValue(Unit)
    }

    fun setUntilFilter(until: Long?) {
        currentUntilDate = until

        dateFilter =
            if (until == null && currentBeyondDate == null)
                NoFilter
            else
                FilterDate(currentBeyondDate, until)

        dumbEvent.postValue(Unit)
    }

    fun setGrapeFilters(filteredGrapes: List<Grape>) {
        selectedGrapes = filteredGrapes

        val grapeFilters: List<WineFilter> = selectedGrapes.map { FilterGrape(it) }

        grapeFilter =
            if (grapeFilters.isNotEmpty())
                grapeFilters.reduce { acc, filterGrape -> acc.orCombine(filterGrape) }
            else NoFilter

        dumbEvent.postValue(Unit)
    }

    fun setReviewFilters(filteredReviews: List<Review>) {
        selectedReviews = filteredReviews

        val reviewFilters: List<WineFilter> = selectedReviews.map { FilterReview(it) }

        reviewFilter =
            if (reviewFilters.isNotEmpty())
                reviewFilters.reduce { acc, filterReview -> acc.orCombine(filterReview) }
            else NoFilter

        dumbEvent.postValue(Unit)
    }

    private fun filter(bottles: List<BoundedBottle>) {
        viewModelScope.launch(Default) {
            val filters = listOf(
                countyFilter, colorFilter, otherFilter, vintageFilter,
                textFilter, priceFilter, dateFilter, grapeFilter, reviewFilter
            )

            val combinedFilters = filters.reduce { acc, wineFilter -> acc.andCombine(wineFilter) }
            val filtered = combinedFilters.meetFilters(bottles)

            _results.postValue(filtered)
        }
    }
}

class A<T> : MutableLiveData<T>() {
    fun trigger() {
        postValue(value)
    }
}
