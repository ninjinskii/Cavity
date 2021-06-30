package com.louis.app.cavity.ui.tasting

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.util.Event

class TastingViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    val futureTastings = repository.getFutureTastings()
    val lastTasting = repository.getLastTasting()
    val friends = repository.getAllFriends()

    // TODO: use Settings
    val temperatureUnit = 0

    var date: Long = System.currentTimeMillis()
    var currentTasting: Tasting? = null

    fun submit(opportunity: String, cellarTemp: Int, fridgeTemp: Int, freezerTemp: Int) {
        currentTasting = Tasting(0, date, opportunity, cellarTemp, fridgeTemp, freezerTemp)
    }
}
