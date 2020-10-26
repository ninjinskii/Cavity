package com.louis.app.cavity.ui.addwine

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.ui.home.WineColor
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AddWineViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private val _wineUpdatedEvent = MutableLiveData<Event<Int>>()
    val wineUpdatedEvent: LiveData<Event<Int>>
        get() = _wineUpdatedEvent

    private val _updatedWine = MutableLiveData<Wine?>()
    val updatedWine: LiveData<Wine?>
        get() = _updatedWine

    private val isEditMode: Boolean
        get() = _updatedWine.value != null

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
        county: County,
        imagePath: String?
    ) {
        if (name.isBlank() || naming.isBlank()) {
            _userFeedback.postOnce(R.string.empty_name_or_naming)
            return
        }

        val colorNumber = when (color) {
            R.id.colorWhite -> WineColor.COLOR_WHITE
            R.id.colorRed -> WineColor.COLOR_RED
            R.id.colorSweet -> WineColor.COLOR_SWEET
            else -> WineColor.COLOR_ROSE
        }

        val wine = Wine(
            0,
            name,
            naming,
            Wine.wineColorToColorNumber(colorNumber),
            cuvee,
            county.countyId,
            isOrganic,
            imagePath ?: ""
        )

        viewModelScope.launch(IO) {
            if (isEditMode) {
                wine.wineId = _updatedWine.value!!.wineId // isEditMode checks for nullability
                repository.updateWine(wine)

                _updatedWine.postValue(null)
                _wineUpdatedEvent.postOnce(R.string.wine_updated)
            } else {
                repository.insertWine(wine)
                _wineUpdatedEvent.postOnce(R.string.wine_added)
            }
        }

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
