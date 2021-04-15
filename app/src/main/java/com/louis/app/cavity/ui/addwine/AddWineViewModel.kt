package com.louis.app.cavity.ui.addwine

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.*
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.WineAndFullNaming
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Naming
import com.louis.app.cavity.model.Wine
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

    private val _updatedWine = MutableLiveData<WineAndFullNaming>()
    val updatedWine: LiveData<WineAndFullNaming>
        get() = _updatedWine

    private val _image = MutableLiveData<String>()
    val image: LiveData<String>
        get() = _image

    private val _countyId = MutableLiveData<Long>()
    val countyId: LiveData<Long>
        get() = _countyId

    private val isEditMode: Boolean
        get() = wineId != 0L

    val namings = _countyId.switchMap { repository.getNamingsForCounty(it) }

    private var wineId = 0L

    var namingId = 0L

    fun start(wineId: Long) {
        this.wineId = wineId

        if (wineId != 0L) {
            viewModelScope.launch(IO) {
                val wineAndNaming = repository.getWineFullNamingByIdNotLive(wineId)
                namingId = wineAndNaming.naming.id

                _countyId.postValue(wineAndNaming.wine.countyId)
                _updatedWine.postValue(wineAndNaming)
                _image.postValue(wineAndNaming.wine.imgPath)
            }
        }
    }

    fun getAllCounties() = repository.getAllCounties()

    fun saveWine(
        name: String,
        cuvee: String,
        isOrganic: Int,
        colorChipId: Int,
        county: County
    ) {
        if (namingId == 0L) {
            _userFeedback.postOnce(R.string.empty_naming)
            return
        }

        val color = when (colorChipId) {
            R.id.colorRed -> 0
            R.id.colorWhite -> 1
            R.id.colorSweet -> 2
            else -> 3
        }

        val wine = Wine(
            wineId,
            name,
            color,
            cuvee,
            isOrganic,
            _image.value ?: "",
            county.id,
            namingId,
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

    fun insertCounty(countyName: String) {
        viewModelScope.launch(IO) {
            val counties = repository.getAllCountiesNotLive()

            try {
                repository.insertCounty(County(name = countyName, prefOrder = counties.size))
                _userFeedback.postOnce(R.string.county_added)
            } catch (e: IllegalArgumentException) {
                _userFeedback.postOnce(R.string.empty_county_name)
            } catch (e: SQLiteConstraintException) {
                _userFeedback.postOnce(R.string.county_already_exists)
            }
        }
    }

    fun insertNaming(naming: String) {
        viewModelScope.launch(IO) {
            try {
                val countyId = _countyId.value ?: throw IllegalStateException()
                repository.insertNaming(Naming(naming = naming, countyId = countyId))
                _userFeedback.postOnce(R.string.naming_added)
            } catch (e: IllegalArgumentException) {
                _userFeedback.postOnce(R.string.base_error)
            } catch (e: SQLiteConstraintException) {
                _userFeedback.postOnce(R.string.naming_already_exists)
            } catch (e: IllegalStateException) {
                _userFeedback.postOnce(R.string.base_error)
            }
        }
    }

    fun setImage(imagePath: String) {
        _image.postValue(imagePath)
    }

    fun setCountyId(countyId: Long) {
        _countyId.postValue(countyId)
        namingId = 0
    }

    private fun reset() {
        wineId = 0
    }
}
