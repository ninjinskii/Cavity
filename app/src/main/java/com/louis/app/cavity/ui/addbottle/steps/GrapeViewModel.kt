package com.louis.app.cavity.ui.addbottle.steps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.relation.QuantifiedBottleGrapeXRef
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class GrapeViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)
    private val qGrapeManager = QuantifiedGrapeManager()

    fun getAllGrapes() = repository.getAllGrapes()

    fun getQGrapesForBottle(bottleId: Long) = repository.getQGrapesForBottle(bottleId)

    fun start(bottleId: Long?) {

    }

    fun addGrape(grape: Grape) = viewModelScope.launch(IO) { repository.insertGrape(grape) }

    fun addQuantifiedGrape(bottleId: Long, grapeId: Long) {
        val checkedQGrape = qGrapeManager.requestAddQGrape(bottleId, grapeId)
        repository.insertQuantifiedGrape(checkedQGrape)
    }

    fun updateQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef, newValue: Int) {
        val checkedQGrape = qGrapeManager.requestUpdateQGrape(qGrape, newValue)
        repository.updateQuantifiedGrape(checkedQGrape)
    }

    fun removeQuantifiedGrape(qGrape: QuantifiedBottleGrapeXRef) {
        qGrapeManager.requestRemoveQGrape(qGrape)
        repository.deleteQuantifiedGrape(qGrape)
    }
}
