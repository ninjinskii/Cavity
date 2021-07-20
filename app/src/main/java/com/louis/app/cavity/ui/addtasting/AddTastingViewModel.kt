package com.louis.app.cavity.ui.addtasting

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.BoundedBottle
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.util.minusAssign
import com.louis.app.cavity.util.plusAssign

class AddTastingViewModel(app: Application) : AndroidViewModel(app) {
    val repository = WineRepository.getInstance(app)

    private val _selectedBottles = MutableLiveData<MutableList<BoundedBottle>>(mutableListOf())
    val selectedBottles: LiveData<MutableList<BoundedBottle>>
        get() = _selectedBottles

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


}
