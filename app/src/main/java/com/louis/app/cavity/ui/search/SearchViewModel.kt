package com.louis.app.cavity.ui.search

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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

    var selectedCounties = emptyList<County>()
    var selectedGrapes = emptyList<Grape>()
    var selectedReviews = emptyList<Review>()
    var selectedFriends = emptyList<Friend>()
    var currentBeyondDate: Long? = null
    var currentUntilDate: Long? = null
    var onFragmentLeaveSavedState : Bundle? = null

    fun getAllCounties() = repository.getAllCounties()

    fun getAllGrapes() = repository.getAllGrapes()

    fun getAllReviews() = repository.getAllReviews()

    fun getAllFriends() = repository.getAllFriends()

    fun submitFilter(wineFilter: WineFilter) {
        globalFilter.value = wineFilter
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

