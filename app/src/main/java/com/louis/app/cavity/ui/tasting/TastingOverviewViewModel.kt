package com.louis.app.cavity.ui.tasting

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.BottleWithTastingActions
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.model.TastingAction
import com.louis.app.cavity.ui.Cavity
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class TastingOverviewViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val tastingId = MutableLiveData<Long>(0)

    val bottles =
        tastingId.switchMap { repository.getBottlesWithTastingActionsForTasting(it) }

    fun start(tastingId: Long) {
        this.tastingId.value = tastingId
    }

    fun setActionIsChecked(tastingAction: TastingAction, isChecked: Boolean) {
        tastingAction.checked = isChecked.toInt()

        viewModelScope.launch(IO) {
            repository.updateTastingAction(tastingAction)
        }
    }

    fun notify(context: Context, bottleWithActions: List<BottleWithTastingActions>) {
        viewModelScope.launch(IO) {
            val tasting = repository.getLastTastingByIdNotLive(tastingId.value ?: return@launch)

            bottleWithActions.forEach { bottle ->
                bottle.tastingActions.forEach { action ->
                    val notification = TastingNotifier.buildNotification(context, tasting, action)
                    TastingNotifier.notify(context, notification)
                }
            }
        }
    }
}
