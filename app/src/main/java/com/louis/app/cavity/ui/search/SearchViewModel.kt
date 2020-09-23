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
import com.louis.app.cavity.ui.search.filters.And
import com.louis.app.cavity.ui.search.filters.FilterRed
import com.louis.app.cavity.ui.search.filters.FilterWhite
import com.louis.app.cavity.ui.search.filters.Or
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.toBoolean
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private var filteredCounties = mutableListOf<County>()
    private var filterConstraint = FilterConstraint()

    fun getAllCountiesNotLive() = repository.getAllCountiesNotLive()

    fun submitCounties(counties: List<County>) {
        L.v("submitCounties")
        filteredCounties.clear()
        filteredCounties.addAll(counties)
        filter()
    }

    fun submitFilterConstraint(chipCheckedState: Map<Int, Boolean>) {
        L.v(chipCheckedState.toString())

        filterConstraint = FilterConstraint(
            chipCheckedState[R.id.chipReadyToDrink] ?: false,
            chipCheckedState[R.id.chipRed] ?: false,
            chipCheckedState[R.id.chipWhite] ?: false,
            chipCheckedState[R.id.chipSweet] ?: false,
            chipCheckedState[R.id.chipRose] ?: false,
            chipCheckedState[R.id.chipOrganic] ?: false
        )

        filter()
    }

    private fun filter() {
        viewModelScope.launch(IO) {
            val winesWithBottles = repository.getWineWithBottlesNotLive()

            val filter = Or(FilterRed(), FilterWhite())

            _results.postValue(filter.meetFilters(winesWithBottles))
        }
    }

    data class FilterConstraint(
        val isReadyToDrink: Boolean = false,
        val isRed: Boolean = false,
        val isWhite: Boolean = false,
        val isSweet: Boolean = false,
        val isRose: Boolean = false,
        val isOrganic: Boolean = false
    )
}
