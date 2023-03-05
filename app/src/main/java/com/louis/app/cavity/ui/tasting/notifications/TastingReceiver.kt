package com.louis.app.cavity.ui.tasting.notifications

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.louis.app.cavity.db.WineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TastingReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TASTING_ID = "com.louis.app.cavity.EXTRA_TASTING_ID"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val repository = WineRepository.getInstance(context.applicationContext as Application)
        val tastingId = intent.getLongExtra(EXTRA_TASTING_ID, -1)

        if (tastingId == -1L) {
            return
        }

        CoroutineScope(SupervisorJob()).launch(IO) {
            val tasting = repository.getTastingById(tastingId) ?: return@launch
            val bottlesWithActions =
                repository.getBottlesWithTastingActionsForTastingNotLive(tastingId)

            bottlesWithActions.forEach { bottle ->
                bottle.tastingActions.forEach {
                    val notification =
                        TastingNotifier.buildNotification(context, tasting, bottle.wine, it)
                    TastingNotifier.notify(context, notification)
                }
            }
        }
    }
}
