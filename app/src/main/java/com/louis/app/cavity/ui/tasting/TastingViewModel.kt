package com.louis.app.cavity.ui.tasting

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.louis.app.cavity.db.WineRepository

class TastingViewModel(app: Application): AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    val futureTastings = repository.getFutureTastings()

    val lastTasting = repository.getLastTasting()
}
