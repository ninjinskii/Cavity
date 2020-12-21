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
import com.louis.app.cavity.util.L
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

    private val _updatedWine = MutableLiveData<Wine>()
    val updatedWine: LiveData<Wine>
        get() = _updatedWine

    private val isEditMode: Boolean
        get() = wineId != 0L

    private var wineId = 0L
    private var image = ""

    fun start(wineId: Long) {
        this.wineId = wineId

        if (wineId != 0L) {
            viewModelScope.launch(IO) {
                val wine = repository.getWineByIdNotLive(wineId)
                image = wine.imgPath
                _updatedWine.postValue(wine)
            }
        }
    }

    fun saveWine(
        name: String,
        naming: String,
        cuvee: String,
        isOrganic: Int,
        color: Int,
        county: County,
    ) {
        val colorNumber = when (color) {
            R.id.colorWhite -> WineColor.COLOR_WHITE
            R.id.colorRed -> WineColor.COLOR_RED
            R.id.colorSweet -> WineColor.COLOR_SWEET
            else -> WineColor.COLOR_ROSE
        }

        val wine = Wine(
            wineId,
            name,
            naming,
            Wine.wineColorToColorNumber(colorNumber),
            cuvee,
            county.countyId,
            isOrganic,
            image
        )

        viewModelScope.launch(IO) {
            if (isEditMode) {
                repository.updateWine(wine)
                _wineUpdatedEvent.postOnce(R.string.wine_updated)
            } else {
                repository.insertWine(wine)
                _wineUpdatedEvent.postOnce(R.string.wine_added)
            }

            reset()
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

    fun setImage(imagePath: String) {
        image = imagePath
    }

    private fun reset() {
        wineId = -1
        image = ""
    }

    fun getAllCounties() = repository.getAllCounties()
}
