package com.louis.app.cavity.ui.tasting

import android.app.Application
import androidx.lifecycle.*
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.model.TastingAction
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class TastingOverviewViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _notificationEvent = MutableLiveData<Event<Pair<Tasting, List<TastingAction>>>>()
    val notificationEvent: LiveData<Event<Pair<Tasting, List<TastingAction>>>>
        get() = _notificationEvent

    private val tastingId = MutableLiveData<Long>(0)

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

    fun prepareNotifications() {
        viewModelScope.launch(IO) {
            val tasting = repository.getLastTastingByIdNotLive(tastingId.value ?: return@launch)
            val actions = bottles.value?.mapNotNull {
                it.tastingActions.firstOrNull()
            }

            _notificationEvent.postOnce(tasting to (actions ?: emptyList()))
        }
    }
}
