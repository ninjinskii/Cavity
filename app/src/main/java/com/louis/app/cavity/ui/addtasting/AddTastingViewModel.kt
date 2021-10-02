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
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.minusAssign
import com.louis.app.cavity.util.plusAssign
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AddTastingViewModel(app: Application) : AndroidViewModel(app) {
    val repository = WineRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private val _selectedBottles = MutableLiveData<MutableList<BoundedBottle>>(mutableListOf())
    val selectedBottles: LiveData<MutableList<BoundedBottle>>
        get() = _selectedBottles

    val tastingBottles = _selectedBottles.switchMap { updateTastingBottles(it) }

    val lastTasting = repository.getLastTasting()
    val friends = repository.getAllFriends()

    var currentTasting: Tasting? = null
    var selectedFriends: List<Long> = emptyList()

    fun submitTasting(
        opportunity: String,
        cellarTemp: Int,
        fridgeTemp: Int,
        freezerTemp: Int,
        date: Long,
        friends: List<Friend>
    ) {
        currentTasting = Tasting(0, date, opportunity, cellarTemp, fridgeTemp, freezerTemp)
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

            generateTastingActions(tastingId, tastingBottles.value)
        }
    }

    fun onBottleStateChanged(bottle: BoundedBottle, isSelected: Boolean) {
        _selectedBottles.let {
            if (isSelected) it += bottle else it -= bottle
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
                    it.wine.color.defaultTemperature,
                    jugTime = 0,
                    isSelected = false,
                    showOccupiedWarning = it.bottle.id in occupiedBottles
                )
            }

            emit(result)
        }

    private suspend fun generateTastingActions(
        tastingId: Long,
        tastingBottles: List<TastingBottle>?
    ) {
        if (tastingBottles == null) {
            return
        }

        val actions = mutableListOf<TastingAction>()

        for (tastingBottle in tastingBottles) {
            val randomFridgeTime: Int = (5..30).random()
            val randomOutFridgeTime: Int = (5..10).random()

            if (tastingBottle.drinkTemp.value < 10) {
                val setToFridgeAction = TastingAction(
                    0,
                    "Mettre votre bouteille au frigo",
                    randomFridgeTime,
                    tastingBottle.bottleId,
                    0
                )

                val outOfFridgeAction = TastingAction(
                    0,
                    "Sortez votre bouteille du frigo",
                    randomOutFridgeTime,
                    tastingBottle.bottleId,
                    0

                )

                actions.add(setToFridgeAction)
                actions.add(outOfFridgeAction)
            } else {
                val outOfCellarAction = TastingAction(
                    0,
                    "Sortez votre bouteille de la cave",
                    0,
                    tastingBottle.bottleId,
                    0
                )

                actions.add(outOfCellarAction)
            }
        }

        repository.insertTastingActions(actions)
    }
}
