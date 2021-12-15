package com.louis.app.cavity.ui.tasting

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.util.Event

class TastingViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    val futureTastings = repository.getFutureTastings()
    val friends = repository.getAllFriends()
}
