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
import com.louis.app.cavity.util.toBoolean
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

    fun setActionIsChecked(context: Context, tastingAction: TastingAction, isChecked: Boolean) {
        tastingAction.checked = isChecked.toInt()

        viewModelScope.launch(IO) {
            repository.updateTastingAction(tastingAction)
        }

        // TODO: add a notification event chennel, and send notifocation form fragment
        notify(context)
    }

    fun notify(context: Context) {
        viewModelScope.launch(IO) {
            val tasting = repository.getLastTastingByIdNotLive(tastingId.value ?: return@launch)

            bottles.value?.forEach { bottle ->
                bottle.tastingActions
                    .firstOrNull { action -> !action.checked.toBoolean() }
                    ?.let {
                        val notification = TastingNotifier.buildNotification(context, tasting, it)
                        TastingNotifier.notify(context, notification)
                    }
            }
        }
    }
}
