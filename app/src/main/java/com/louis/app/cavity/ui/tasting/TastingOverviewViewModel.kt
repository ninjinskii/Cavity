package com.louis.app.cavity.ui.tasting

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.BoundedTasting
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.TastingAction
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class TastingOverviewViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)
    private val tastingId = MutableLiveData<Long>(0)

    private val _tastingConfirmed = MutableLiveData<Event<Unit>>()
    val tastingConfirmed: LiveData<Event<Unit>>
        get() = _tastingConfirmed

    val bottles =
        tastingId.switchMap { repository.getBottlesWithTastingActionsForTasting(it) }

    fun start(tastingId: Long) {
        this.tastingId.value = tastingId
    }

    fun setActionIsChecked(tastingAction: TastingAction, isChecked: Boolean) {
        tastingAction.done = isChecked.toInt()

        viewModelScope.launch(IO) {
            repository.updateTastingAction(tastingAction)
        }
    }

    fun updateBottleTasting(bottle: Bottle, tastingId: Long?) {
        bottle.tastingId = tastingId

        viewModelScope.launch(IO) {
            repository.updateBottle(bottle)
        }
    }

    fun confirmTasting() {
        viewModelScope.launch(IO) {
            val boundedTasting =
                repository.getBoundedTastingById(tastingId.value ?: 0) ?: return@launch

            boundedTasting.tasting.done = true
            repository.updateTasting(boundedTasting.tasting)

            updateStocks(boundedTasting)

            _tastingConfirmed.postOnce(Unit)
        }
    }

    private suspend fun updateStocks(boundedTasting: BoundedTasting) {
        val (tasting, _, friends) = boundedTasting
        bottles.value?.forEach { bottleWithTastingAction ->
            val entry = HistoryEntry(
                id = 0,
                tasting.date,
                bottleId = bottleWithTastingAction.bottle.id,
                tasting.id,
                comment = "",
                type = 4,
                favorite = 0
            )

            repository.insertHistoryEntryAndFriends(entry, friends.map { it.id })
        }
    }
}
