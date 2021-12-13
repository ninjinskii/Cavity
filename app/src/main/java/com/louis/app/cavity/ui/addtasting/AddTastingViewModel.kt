package com.louis.app.cavity.ui.addtasting

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.BoundedBottle
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.model.TastingAction
import com.louis.app.cavity.model.TastingBottle
import com.louis.app.cavity.util.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddTastingViewModel(app: Application) : AndroidViewModel(app) {
    val repository = WineRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private val _notificationEvent = MutableLiveData<Event<Tasting>>()
    val notificationEvent: LiveData<Event<Tasting>>
        get() = _notificationEvent

    private val _selectedBottles = MutableLiveData<MutableList<BoundedBottle>>(mutableListOf())
    val selectedBottles: LiveData<MutableList<BoundedBottle>>
        get() = _selectedBottles

    val tastingBottles = _selectedBottles.switchMap { updateTastingBottles(it) }

    val lastTasting = repository.getLastTasting()
    val friends = repository.getAllFriends()

    var currentTasting: Tasting? = null
    var selectedFriends: List<Long> = emptyList()

    fun submitTasting(opportunity: String, isMidday: Boolean, date: Long, friends: List<Friend>) {
        currentTasting = Tasting(0, date, isMidday, opportunity)
        selectedFriends = friends.map { it.id }
    }

    fun saveTasting() {
        val tasting = currentTasting

        if (tasting == null) {
            _userFeedback.postOnce(R.string.base_error)
            return
        }

        viewModelScope.launch(IO) {
            val bottleIds = _selectedBottles.value?.map { it.bottle.id } ?: emptyList()
            val tastingId = repository.insertTasting(tasting)

            repository.boundBottlesToTasting(tastingId, bottleIds)
            repository.insertTastingFriendXRef(tastingId, selectedFriends)

            generateTastingActions(tasting, tastingBottles.value)
        }
    }

    fun onBottleStateChanged(bottle: BoundedBottle, isSelected: Boolean) {
        _selectedBottles.let {
            if (isSelected) it += bottle else it -= bottle
        }
    }

    fun onBottleShouldJugChanged(bottleId: Long, shouldJug: Boolean) {
        tastingBottles.value?.let { bottles ->
            val tastingBottle = bottles.find { it.bottleId == bottleId }
            tastingBottle?.shouldJug = shouldJug.toInt()
        }
    }

    fun onBottleShouldFridgeChanged(bottleId: Long, shouldFridge: Boolean) {
        tastingBottles.value?.let { bottles ->
            val tastingBottle = bottles.find { it.bottleId == bottleId }
            tastingBottle?.shouldJug = shouldFridge.toInt()
        }
    }

    private fun updateTastingBottles(selectedBottles: List<BoundedBottle>) =
        liveData(IO) {
            val bottleIds = selectedBottles.map { it.bottle.id }
            val occupiedBottles = repository.getTastingBottleIdsIn(bottleIds)

            val result = selectedBottles.map {
                TastingBottle(
                    it.bottle.id,
                    it.wine,
                    it.bottle.vintage,
                    it.bottle.bottleSize,
                    showOccupiedWarning = it.bottle.id in occupiedBottles
                )
            }

            emit(result)
        }

    private suspend fun generateTastingActions(
        tasting: Tasting,
        tastingBottles: List<TastingBottle>?
    ) {
        if (tastingBottles == null) {
            return
        }

        val occupiedBottles = tastingBottles
            .filter { it.showOccupiedWarning }
            .map { it.bottleId }

        if (occupiedBottles.isNotEmpty()) {
            cleanObsoleteTastingActions(occupiedBottles)
        }

        withContext(Default) {
            val actions = mutableListOf<TastingAction>()

            for (tastingBottle in tastingBottles) {
                if (tastingBottle.shouldFridge.toBoolean()) {
                    val action = TastingAction(
                        0,
                        TastingAction.Action.SET_TO_FRIDGE,
                        tastingBottle.bottleId,
                        false.toInt()
                    )

                    actions += action
                }

                if (tastingBottle.shouldJug.toBoolean()) {
                    val action = TastingAction(
                        0,
                        TastingAction.Action.SET_TO_JUG,
                        tastingBottle.bottleId,
                        false.toInt()
                    )

                    actions += action
                }
            }

            withContext(IO) {
                repository.insertTastingActions(actions)
                _notificationEvent.postOnce(tasting)
            }
        }
    }

    private suspend fun cleanObsoleteTastingActions(tastingBottleIds: List<Long>) {
        withContext(IO) {
            tastingBottleIds.forEach {
                repository.deleteTastingActionsForBottle(it)
            }
        }
    }
}
