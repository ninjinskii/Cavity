package com.louis.app.cavity.ui.addtasting

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.dao.BoundedBottle
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.FriendRepository
import com.louis.app.cavity.domain.repository.TastingRepository
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.model.TastingAction
import com.louis.app.cavity.model.TastingBottle
import com.louis.app.cavity.ui.BaseViewModel
import com.louis.app.cavity.util.toBoolean
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface AddTastingEvent {
    data class UserFeedback(@StringRes val resId: Int) : AddTastingEvent
    data class TastingSaved(val tasting: Tasting) : AddTastingEvent
    data class CancelTastingAlarms(val tastings: List<Tasting>) : AddTastingEvent
}

data class AddTastingUiState(
    val selectedBottles: List<BoundedBottle> = emptyList(),
    val tastingBottles: List<TastingBottle> = emptyList(),
    val friends: List<Friend> = emptyList()
)

class AddTastingViewModel(app: Application) : BaseViewModel<AddTastingUiState, AddTastingEvent>(app, AddTastingUiState()) {
    private val bottleRepository = BottleRepository.getInstance(app)
    private val tastingRepository = TastingRepository.getInstance(app)
    private val friendRepository = FriendRepository.getInstance(app)

    private val _selectedBottles = MutableStateFlow<List<BoundedBottle>>(emptyList())
    val selectedBottles: StateFlow<List<BoundedBottle>> = _selectedBottles.asStateFlow()

    private var currentTasting: Tasting? = null
    private var selectedFriends: List<Long> = emptyList()
    var tastingDate: Long = System.currentTimeMillis()

    init {
        friendRepository.getAllFriends()
            .onEach { viewState = viewState.copy(friends = it) }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            _selectedBottles.collect { bottles ->
                val bottleIds = bottles.map { it.bottle.id }
                val occupiedBottles = withContext(IO) {
                    bottleRepository.getTastingBottleIdsIn(bottleIds)
                }
                val tastingBottles = bottles.map {
                    TastingBottle(
                        it.bottle.id,
                        it.wine,
                        it.bottle.vintage,
                        it.bottle.bottleSize,
                        showOccupiedWarning = it.bottle.id in occupiedBottles
                    )
                }
                viewState = viewState.copy(selectedBottles = bottles, tastingBottles = tastingBottles)
            }
        }
    }

    fun submitTasting(
        opportunity: String,
        isMidday: Boolean,
        friends: List<Friend>
    ): Boolean {
        if (friends.isEmpty()) {
            emitEvent(AddTastingEvent.UserFeedback(R.string.no_friend))
            return false
        }

        currentTasting = Tasting(0, tastingDate, isMidday, opportunity)
        selectedFriends = friends.map { it.id }

        return true
    }

    fun saveTasting() {
        val tasting = currentTasting

        if (tasting == null) {
            emitEvent(AddTastingEvent.UserFeedback(R.string.base_error))
            return
        }

        viewModelScope.launch(IO) {
            val bottleIds = _selectedBottles.value.map { it.bottle.id }
            val tastingId = tastingRepository.insertTasting(tasting)

            currentTasting = currentTasting!!.copy(id = tastingId)

            tastingRepository.run {
                transaction {
                    bottleRepository.boundBottlesToTasting(tastingId, bottleIds)
                    insertTastingFriendXRef(tastingId, selectedFriends)
                    generateTastingActions(currentTasting!!, viewState.tastingBottles)
                }
            }
        }
    }

    fun onBottleStateChanged(bottle: BoundedBottle, isSelected: Boolean) {
        val current = _selectedBottles.value.toMutableList()
        if (isSelected) current.add(bottle) else current.remove(bottle)
        _selectedBottles.value = current
    }

    private suspend fun generateTastingActions(
        tasting: Tasting,
        tastingBottles: List<TastingBottle>
    ) {
        val occupiedBottles = tastingBottles
            .filter { it.showOccupiedWarning }
            .map { it.bottleId }

        if (occupiedBottles.isNotEmpty()) {
            cleanTastings(occupiedBottles)
        }

        val actions = mutableListOf<TastingAction>()

        for (tastingBottle in tastingBottles) {
            tastingRepository.deleteTastingActionsForBottle(tastingBottle.bottleId)

            if (tastingBottle.shouldFridge.toBoolean()) {
                actions += TastingAction(0, TastingAction.Action.SET_TO_FRIDGE, tastingBottle.bottleId, false.toInt())
            }

            if (tastingBottle.shouldJug.toBoolean()) {
                actions += TastingAction(0, TastingAction.Action.SET_TO_JUG, tastingBottle.bottleId, false.toInt())
            }

            if (tastingBottle.shouldUncork.toBoolean()) {
                actions += TastingAction(0, TastingAction.Action.UNCORK, tastingBottle.bottleId, false.toInt())
            }
        }

        tastingRepository.insertTastingActions(actions)
        emitEvent(AddTastingEvent.TastingSaved(tasting))
    }

    private suspend fun cleanTastings(tastingBottleIds: List<Long>) {
        withContext(IO) {
            val emptyTastings = tastingRepository.getEmptyTastings()

            if (emptyTastings.isNotEmpty()) {
                emitEvent(AddTastingEvent.CancelTastingAlarms(emptyTastings))
                tastingRepository.deleteTastings(emptyTastings)
            }

            tastingBottleIds.forEach {
                tastingRepository.deleteTastingActionsForBottle(it)
            }
        }
    }
}
