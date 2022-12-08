package com.louis.app.cavity.ui.addwine

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.WineColor
import com.louis.app.cavity.util.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val _image = MutableLiveData<String>()
    val image: LiveData<String>
        get() = _image

    private val _countyId = MutableLiveData<Long>()

    private val isEditMode: Boolean
        get() = wineId != 0L

    val namings = _countyId.switchMap { repository.getNamingsForCounty(it) }

    private var wineId = 0L

    fun start(wineId: Long) {
        this.wineId = wineId

        if (wineId != 0L) {
            viewModelScope.launch(IO) {
                val wine = repository.getWineByIdNotLive(wineId)

                _countyId.postValue(wine.countyId)
                _updatedWine.postValue(wine)
                _image.postValue(wine.imgPath)
            }
        }
    }

    fun getAllCounties() = repository.getAllCounties()

    fun saveWine(
        name: String,
        naming: String,
        cuvee: String,
        isOrganic: Int,
        colorChipId: Int,
        county: County?
    ) {
        if (county == null) {
            _userFeedback.postOnce(R.string.no_county)
            return
        }

        val color = when (colorChipId) {
            R.id.colorRed -> WineColor.RED
            R.id.colorWhite -> WineColor.WHITE
            R.id.colorSweet -> WineColor.SWEET
            else /* R.id.colorRose */ -> WineColor.ROSE
        }

        val wine = Wine(
            wineId,
            name,
            naming,
            color,
            cuvee,
            isOrganic,
            _image.value ?: "",
            county.id
        )

        viewModelScope.launch(IO) {
            val duplicate = getSimilarWineIfAny(wine)

            L.v(duplicate.toString())

            if (duplicate != null) {
                when {
                    duplicate.hidden.toBoolean() && !isEditMode -> {
                        repository.updateWine(duplicate.copy(hidden = false.toInt()))
                        _wineUpdatedEvent.postOnce(R.string.wine_already_exists_emergence)
                    }
                    else -> _userFeedback.postOnce(R.string.wine_already_exists)
                }

                return@launch
            }

            /*      if (duplicate != null && wine.hidden.toBoolean()) {
                      _wineUpdatedEvent.postOnce(R.string.wine_already_exists_emergence)
                      return@launch
                  }

                  if (duplicate != null && !wine.hidden.toBoolean()) {
                      _userFeedback.postOnce(R.string.wine_already_exists)
                      return@launch
                  }*/

            when {
                isEditMode -> {
                    repository.updateWine(wine)
                    _wineUpdatedEvent.postOnce(R.string.wine_updated)
                    reset()
                }

                else -> {
                    repository.insertWine(wine)
                    _wineUpdatedEvent.postOnce(R.string.wine_added)
                    reset()
                }
            }
        }
    }

    fun setImage(imagePath: String) {
        _image.postValue(imagePath)
    }

    fun setCountyId(countyId: Long?) {
        countyId?.let {
            _countyId.postValue(it)
        }
    }

    private fun reset() {
        wineId = 0
    }

    private suspend fun getSimilarWineIfAny(wine: Wine): Wine? {
        val hiddenWines = repository.getWineByColorAndOrganic(wine.color, wine.isOrganic)

        return withContext(Default) {
            for (hiddenWine in hiddenWines) {
                val hasCloseNames = levenshtein(hiddenWine.name, wine.name) <= 3
                val hasCloseNamings = levenshtein(hiddenWine.naming, wine.naming) <= 3
                val isSelf = hiddenWine.id == wineId && isEditMode

                if (hasCloseNames && hasCloseNamings && !isSelf) {
                    return@withContext hiddenWine
                }
            }

            return@withContext null
        }
    }

    private fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length + 1
        val rhsLength = rhs.length + 1

        var cost = Array(lhsLength) { it }
        var newCost = Array(lhsLength) { 0 }

        for (i in 1 until rhsLength) {
            newCost[0] = i

            for (j in 1 until lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = costInsert.coerceAtMost(costDelete).coerceAtMost(costReplace)
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[lhsLength - 1]
    }
}
