package com.louis.app.cavity.ui.addwine

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.material.chip.Chip
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.ui.home.WineColor
import com.louis.app.cavity.ui.search.filters.FilterOrganic
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.showSnackbar
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AddWineViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private val _wineUpdatedEvent = MutableLiveData<Event<Unit>>()
    val wineUpdatedEvent: LiveData<Event<Unit>>
        get() = _wineUpdatedEvent

    private val _updatedWine = MutableLiveData<Wine>()
    val updatedWine: LiveData<Wine>
        get() = _updatedWine

    fun startEditMode(wineId: Long) {
        viewModelScope.launch(IO) {
            val wine = repository.getWineByIdNotLive(wineId)
            _updatedWine.postValue(wine)
        }
    }

    fun saveWine(
        name: String,
        naming: String,
        cuvee: String,
        isOrganic: Int,
        color: Int,
        checkedCounty: County,
    ) {
//        if (countyChipGroup.checkedChipId == View.NO_ID) {
//            coordinator.showSnackbar(R.string.no_county)
//            nestedScrollView.smoothScrollTo(0, 0)
//        } else if (name.isBlank() || naming.isBlank()) {
//            coordinator.showSnackbar(R.string.empty_name_or_naming)
//            if (name.isBlank()) nameLayout.error = getString(R.string.required_field)
//            if (naming.isBlank()) namingLayout.error = getString(R.string.required_field)
//        } else {
//            nameLayout.error = null
//            namingLayout.error = null
//
//            val county = countyChipGroup
//                .findViewById<Chip>(checkedChipId)
//                .getTag(R.string.tag_chip_id) as County
//
//            val wine = Wine(
//                0,
//                name,
//                naming,
//                Wine.wineColorToColorNumber(getWineColor(color)),
//                cuvee,
//                county.countyId,
//                isOrganic,
//                wineImagePath ?: ""
//            )
//
//            if (!editMode) {
//                addWineViewModel.addWine(wine)
//            } else {
//                wine.apply { wineId = addWineViewModel.editWine!!.wineId }
//                    .also { addWineViewModel.updateWine(wine) }
//            }

        //findNavController().popBackStack()
        //}
    }

    fun addWine(wine: Wine) = viewModelScope.launch(IO) {
        repository.insertWine(wine)
    }

    fun addCounty(countyName: String) {
        viewModelScope.launch(IO) {
            if (countyName.isNotEmpty()) {
                val counties = repository.getAllCountiesNotLive().map { it.name }

                if (countyName !in counties) {
                    repository.insertCounty(County(name = countyName, prefOrder = counties.size))
                } else {
                    _userFeedback.postOnce(R.string.county_already_exist)
                }
            } else {
                _userFeedback.postOnce(R.string.empty_county_name)
            }
        }
    }

    fun getAllCounties() = repository.getAllCounties()
}
