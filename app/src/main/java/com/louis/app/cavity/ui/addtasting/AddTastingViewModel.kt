package com.louis.app.cavity.ui.addtasting

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.BoundedBottle
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.model.TastingBottle
import com.louis.app.cavity.util.minusAssign
import com.louis.app.cavity.util.plusAssign
import kotlinx.coroutines.Dispatchers.Default

class AddTastingViewModel(app: Application) : AndroidViewModel(app) {
    val repository = WineRepository.getInstance(app)

    private val _selectedBottles = MutableLiveData<MutableList<BoundedBottle>>(mutableListOf())
    val selectedBottles: LiveData<MutableList<BoundedBottle>>
        get() = _selectedBottles

    val tastingBottles = _selectedBottles.switchMap { updateTastingBottles(it) }

    val lastTasting = repository.getLastTasting()
    val friends = repository.getAllFriends()

    var currentTasting: Tasting? = null

    fun submitTasting(
        opportunity: String,
        cellarTemp: Int,
        fridgeTemp: Int,
        freezerTemp: Int,
        date: Long
    ) {
        currentTasting = Tasting(0, date, opportunity, cellarTemp, fridgeTemp, freezerTemp)
    }

    fun onBottleStateChanged(bottle: BoundedBottle, isSelected: Boolean) {
        _selectedBottles.let {
            if (isSelected) it += bottle else it -= bottle
        }
    }

    private fun updateTastingBottles(selectedBottles: List<BoundedBottle>) =
        liveData(Default) {
            val result = selectedBottles.map {
                TastingBottle(
                    it.bottle.id,
                    it.wine,
                    it.bottle.vintage,
                    it.wine.color.defaultTemperature,
                    jugTime = null,
                    isSelected = false
                )
            }

            emit(result)
        }
}
