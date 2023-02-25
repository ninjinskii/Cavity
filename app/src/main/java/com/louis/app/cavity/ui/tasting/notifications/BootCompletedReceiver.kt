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

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val repository = WineRepository.getInstance(context.applicationContext as Application)

            CoroutineScope(SupervisorJob()).launch(IO) {
                repository.getAllTastingsNotLive().forEach {
                    TastingAlarmScheduler.scheduleTastingAlarm(context, it)
                }
            }
        }
    }
}
