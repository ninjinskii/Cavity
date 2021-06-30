package com.louis.app.cavity.ui.tasting

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

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

    fun insertFriend(nameLastName: String) {
        viewModelScope.launch(IO) {
            try {
                repository.insertFriend(Friend(0, nameLastName, ""))
                _userFeedback.postOnce(R.string.friend_added)
            } catch (e: IllegalArgumentException) {
                _userFeedback.postOnce(R.string.input_error)
            } catch (e: SQLiteConstraintException) {
                _userFeedback.postOnce(R.string.friend_already_exists)
            }
        }
    }
}
