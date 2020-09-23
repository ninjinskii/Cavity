package com.louis.app.cavity.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.louis.app.cavity.db.CavityDatabase
import com.louis.app.cavity.db.WineRepository

class SearchViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository(CavityDatabase.getInstance(app))

    fun getAllCountiesNotLive() = repository.getAllCountiesNotLive()

    fun getWineWithBottles() = repository.getWineWithBottles()
}
