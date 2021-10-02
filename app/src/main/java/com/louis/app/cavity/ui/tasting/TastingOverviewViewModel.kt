package com.louis.app.cavity.ui.tasting

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.louis.app.cavity.db.WineRepository

class TastingOverviewViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val tastingId = MutableLiveData<Long>(0)

    val bottles =
        tastingId.switchMap { repository.getBottlesWithTastingActionsForTasting(it) }

    fun start(tastingId: Long) {
        this.tastingId.value = tastingId
    }

    fun setActionIsChecked(actionId: Long, isChecked: Boolean) {

    }
}
