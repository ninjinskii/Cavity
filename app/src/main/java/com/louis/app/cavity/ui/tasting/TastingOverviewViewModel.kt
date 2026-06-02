package com.louis.app.cavity.ui.tasting

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.dao.BoundedTasting
import com.louis.app.cavity.db.dao.BottleWithTastingActions
import com.louis.app.cavity.domain.history.HistoryEntryType
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.HistoryRepository
import com.louis.app.cavity.domain.repository.TastingRepository
import com.louis.app.cavity.domain.repository.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.TastingAction
import com.louis.app.cavity.ui.BaseViewModel
import com.louis.app.cavity.ui.notifications.NotificationBuilder
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed interface TastingOverviewEvent {
    data object TastingConfirmed : TastingOverviewEvent
}

data class TastingOverviewUiState(val bottles: List<BottleWithTastingActions> = emptyList())

class TastingOverviewViewModel(app: Application) :
    BaseViewModel<TastingOverviewUiState, TastingOverviewEvent>(app, TastingOverviewUiState()) {

    private val wineRepository = WineRepository.getInstance(app)
    private val bottleRepository = BottleRepository.getInstance(app)
    private val historyRepository = HistoryRepository.getInstance(app)
    private val tastingRepository = TastingRepository.getInstance(app)

    private val _tastingId = MutableStateFlow(0L)

    init {
        @OptIn(ExperimentalCoroutinesApi::class)
        _tastingId
            .flatMapLatest { tastingRepository.getBottlesWithTastingActionsForTasting(it) }
            .onEach { viewState = viewState.copy(bottles = it) }
            .launchIn(viewModelScope)
    }

    fun start(tastingId: Long) {
        _tastingId.value = tastingId
    }

    fun setActionIsChecked(tastingAction: TastingAction, isChecked: Boolean) {
        tastingAction.done = isChecked.toInt()

        viewModelScope.launch(IO) {
            tastingRepository.updateTastingAction(tastingAction)
        }
    }

    fun updateBottleTasting(bottle: Bottle, tastingId: Long?) {
        bottle.tastingId = tastingId

        viewModelScope.launch(IO) {
            bottleRepository.updateBottle(bottle)
        }
    }

    fun updateBottleComment(bottle: Bottle, comment: String) {
        viewModelScope.launch(IO) {
            bottleRepository.updateBottle(bottle.copy(tastingTasteComment = comment))
        }
    }

    fun requestNotificationsForTastingAction(context: Context, tastingAction: TastingAction) {
        viewModelScope.launch(IO) {
            val bottle = bottleRepository.getBottleByIdNotLive(tastingAction.bottleId)
            val wine = wineRepository.getWineByIdNotLive(bottle.wineId)
            val tasting = tastingRepository.getTastingById(bottle.tastingId ?: return@launch)

            val notification = NotificationBuilder.buildTastingNotification(
                context,
                tasting ?: return@launch,
                wine,
                tastingAction
            )

            NotificationBuilder.notify(context, notification)
        }
    }

    fun confirmTasting() {
        viewModelScope.launch(IO) {
            val boundedTasting =
                tastingRepository.getBoundedTastingById(_tastingId.value) ?: return@launch

            boundedTasting.tasting.done = true
            tastingRepository.updateTasting(boundedTasting.tasting)

            updateStocks(boundedTasting)

            emitEvent(TastingOverviewEvent.TastingConfirmed)
        }
    }

    private suspend fun updateStocks(boundedTasting: BoundedTasting) {
        val (tasting, bottles, friends) = boundedTasting

        bottles
            .filter { it.consumed == false.toInt() }
            .forEach { bottle ->
                val entry = HistoryEntry(
                    id = 0,
                    tasting.date,
                    bottle.id,
                    tasting.id,
                    comment = bottle.tastingTasteComment,
                    type = HistoryEntryType.TASTING,
                    favorite = 0
                )

                bottleRepository.run {
                    tastingRepository.transaction {
                        consumeBottle(bottle.id)
                        historyRepository.insertHistoryEntry(entry, friends.map { it.id })
                        tastingRepository.deleteTastingActionsForBottle(bottle.id)
                    }
                }
            }
    }
}
