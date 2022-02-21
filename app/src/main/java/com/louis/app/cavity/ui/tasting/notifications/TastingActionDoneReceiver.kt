package com.louis.app.cavity.ui.tasting.notifications

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Can't find any other way than GlobalScope to have suspending feature inside BroadcastReceiver
@DelicateCoroutinesApi
class TastingActionDoneReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TASTING_ACTION_ID = "com.louis.app.cavity.EXTRA_TASTING_ACTION_ID"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val repository = WineRepository.getInstance(context.applicationContext as Application)
        val tastingActionId = intent.getLongExtra(EXTRA_TASTING_ACTION_ID, -1)

        if (tastingActionId == -1L) {
            return
        }

        GlobalScope.launch(IO) {
            val tastingAction = repository.getTastingActionById(tastingActionId)
            tastingAction.done = true.toInt()
            repository.updateTastingAction(tastingAction)

            TastingNotifier.cancelNotification(context, tastingActionId.toInt())
        }
    }
}
