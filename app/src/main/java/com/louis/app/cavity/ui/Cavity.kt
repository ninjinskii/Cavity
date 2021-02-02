package com.louis.app.cavity.ui

import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import java.io.File

class Cavity : Application() {

    companion object {
        const val PHOTOS_DIRECTORY = "/wines/"
        const val FRIENDS_DIRECTORY = "/friends/"
        const val DOCUMENTS_DIRECTORY = "/pdfs/" // Used when retrieving a DB from back end
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    }
}
