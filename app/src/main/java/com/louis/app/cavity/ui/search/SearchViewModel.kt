package com.louis.app.cavity.ui.search

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
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
import com.louis.app.cavity.ui.search.filters.*
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.combineAsync
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch

class SearchViewModel(app: Application) : AndroidViewModel(app) {
    private val countyRepository = CountyRepository.getInstance(app)
    private val bottleRepository = BottleRepository.getInstance(app)
    private val grapeRepository = GrapeRepository.getInstance(app)
    private val reviewRepository = ReviewRepository.getInstance(app)
    private val friendRepository = FriendRepository.getInstance(app)
    private val tagRepository = TagRepository.getInstance(app)

    private val _sort = MutableLiveData(Event(Sort(SortCriteria.NONE)))
    val sort: LiveData<Event<Sort>>
        get() = _sort

    private val globalFilter = MutableLiveData<WineFilter>(FilterConsumed(false))
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

    val results: LiveData<List<BoundedBottle>> = _sort.switchMap {
        bottleRepository
            .getBoundedBottles()
            .combineAsync(globalFilter) { receiver, bottles, filter ->
                filterAndSort(receiver, bottles, filter, it.peekContent())
            }
    }

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

    fun getAllTags() = tagRepository.getAllTags().asLiveData()

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
        globalFilter.value =
            searchControllerMap.values.reduce { acc, wFilter -> acc.andCombine(wFilter) }
    }

    fun submitFilters(wineFilters: Map<Int, WineFilter>) {
        wineFilters.forEach { (viewControllerId, wineFilter) ->
            searchControllerMap[viewControllerId] = wineFilter
        }

        globalFilter.value =
            searchControllerMap.values.reduce { acc, wFilter -> acc.andCombine(wFilter) }
    }

    fun submitSortOrder(sort: Sort) {
        this._sort.postOnce(sort)
    }

    private fun filterAndSort(
        receiver: MutableLiveData<List<BoundedBottle>>,
        bottles: List<BoundedBottle>,
        filter: WineFilter,
        sort: Sort
    ) {
        viewModelScope.launch(Default) {
            var filtered = filter.meetFilters(bottles)

            if (sort.criteria != SortCriteria.NONE) {
                filtered = sortWithNullLast(
                    filtered,
                    sort.reversed,
                    sort.criteria.selector
                )
            }

            receiver.postValue(filtered)
        }
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

