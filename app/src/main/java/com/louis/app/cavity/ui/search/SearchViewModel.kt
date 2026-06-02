package com.louis.app.cavity.ui.search

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.dao.BoundedBottle
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.CountyRepository
import com.louis.app.cavity.domain.repository.FriendRepository
import com.louis.app.cavity.domain.repository.GrapeRepository
import com.louis.app.cavity.domain.repository.ReviewRepository
import com.louis.app.cavity.domain.repository.TagRepository
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.model.Tag
import com.louis.app.cavity.ui.BaseViewModel
import com.louis.app.cavity.ui.search.filters.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SearchUiState(
    val sort: Sort = Sort(SortCriteria.NONE),
    val results: List<BoundedBottle> = emptyList()
)

class SearchViewModel(app: Application) : BaseViewModel<SearchUiState, Nothing>(app, SearchUiState()) {
    private val countyRepository = CountyRepository.getInstance(app)
    private val bottleRepository = BottleRepository.getInstance(app)
    private val grapeRepository = GrapeRepository.getInstance(app)
    private val reviewRepository = ReviewRepository.getInstance(app)
    private val friendRepository = FriendRepository.getInstance(app)
    private val tagRepository = TagRepository.getInstance(app)

    private val _sort = MutableStateFlow(Sort(SortCriteria.NONE))
    val sortFlow: StateFlow<Sort> = _sort.asStateFlow()

    private val _globalFilter = MutableStateFlow<WineFilter>(FilterConsumed(false))
    private val searchControllerMap = mutableMapOf(
        R.id.searchView to NoFilter,
        R.id.chipSelected to NoFilter,
        R.id.countyChipGroup to NoFilter,
        R.id.colorChipGroup to NoFilter,
        R.id.otherChipGroup to FilterConsumed(false),
        R.id.vintageSlider to NoFilter,
        R.id.beyondLayout to NoFilter,
        R.id.untilLayout to NoFilter,
        R.id.priceSlider to NoFilter,
        R.id.alcoholSlider to NoFilter,
        R.id.grapeChipGroup to NoFilter,
        R.id.reviewChipGroup to NoFilter,
        R.id.friendChipGroup to NoFilter,
        R.id.tagChipGroup to NoFilter,
        R.id.storageLocation to NoFilter,
        R.id.rbGroupSize to NoFilter
    )

    val results: StateFlow<List<BoundedBottle>> = combine(_sort, _globalFilter) { sort, filter ->
        sort to filter
    }.flatMapLatest { (sort, filter) ->
        bottleRepository.getBoundedBottles().map { bottles ->
            filterAndSort(bottles, filter, sort)
        }
    }
        .flowOn(Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var selectedCounties = emptyList<County>()
    var selectedGrapes = emptyList<Grape>()
    var selectedReviews = emptyList<Review>()
    var selectedFriends = emptyList<Friend>()
    var selectedTags = emptyList<Tag>()
    var currentBeyondDate: Long? = null
    var currentUntilDate: Long? = null
    var onFragmentLeaveSavedState: Bundle? = null

    var friendFilterMode = 1
        private set(value) {
            field = if (value !in 0..2) 0 else value
        }

    fun getAllCounties() = countyRepository.getAllCounties()

    fun getAllGrapes() = grapeRepository.getAllGrapes()

    fun getAllReviews() = reviewRepository.getAllReviews()

    fun getAllFriends() = friendRepository.getAllFriends()

    fun getAllTags() = tagRepository.getAllTags()

    fun getAllStorageLocations(clearText: String) = bottleRepository.getAllStorageLocations().map {
        listOf(clearText) + it
    }

    fun cycleFriendFilterMode(): Int {
        return ++friendFilterMode
    }

    fun shouldShowConsumedAndUnconsumedBottles(): Boolean {
        return selectedFriends.isNotEmpty()
    }

    fun submitFilter(viewControllerId: Int, wineFilter: WineFilter) {
        searchControllerMap[viewControllerId] = wineFilter
        _globalFilter.value =
            searchControllerMap.values.reduce { acc, wFilter -> acc.andCombine(wFilter) }
    }

    fun submitFilters(wineFilters: Map<Int, WineFilter>) {
        wineFilters.forEach { (viewControllerId, wineFilter) ->
            searchControllerMap[viewControllerId] = wineFilter
        }

        _globalFilter.value =
            searchControllerMap.values.reduce { acc, wFilter -> acc.andCombine(wFilter) }
    }

    fun submitSortOrder(sort: Sort) {
        _sort.value = sort
    }

    private fun filterAndSort(
        bottles: List<BoundedBottle>,
        filter: WineFilter,
        sort: Sort
    ): List<BoundedBottle> {
        var filtered = filter.meetFilters(bottles)

        if (sort.criteria != SortCriteria.NONE) {
            filtered = sortWithNullLast(filtered, sort.reversed, sort.criteria.selector)
        }

        return filtered
    }

    private fun sortWithNullLast(
        list: List<BoundedBottle>,
        reversed: Boolean,
        selector: (BoundedBottle) -> Comparable<*>?
    ): List<BoundedBottle> {
        return list.sortedWith { a, b ->
            val va = selector(a)
            val vb = selector(b)

            when {
                va == null && vb == null -> 0
                va == null -> 1
                vb == null -> -1
                else -> {
                    @Suppress("UNCHECKED_CAST")
                    val cmp = (va as Comparable<Any>).compareTo(vb as Comparable<Any>)
                    if (reversed) -cmp else cmp
                }
            }
        }
    }
}

