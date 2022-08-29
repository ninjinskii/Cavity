package com.louis.app.cavity.ui

import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate.*
import com.louis.app.cavity.ui.tasting.notifications.TastingNotifier

class Cavity : Application() {
    override fun onCreate() {
        super.onCreate()
        val isOreoOrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        val mode = if (isOreoOrHigher) MODE_NIGHT_FOLLOW_SYSTEM else MODE_NIGHT_YES

        setDefaultNightMode(mode)

        TastingNotifier.createNotificationChannel(this)
    }
}
