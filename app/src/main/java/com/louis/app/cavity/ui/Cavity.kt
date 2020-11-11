package com.louis.app.cavity.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class Cavity : Application() {
    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}
