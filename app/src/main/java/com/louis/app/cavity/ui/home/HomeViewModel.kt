package com.louis.app.cavity.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.db.CavityDatabase
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Wine

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository(
        CavityDatabase.getInstance(app).wineDao(),
        CavityDatabase.getInstance(app).bottleDao()
    )

    // Consider using a transformation to get child vintage with wines
    fun getAllWines() = repository.getAllWines()

//    Transformations.map(repository.getAllWines()) {
//        it.map { wine ->
//            wine.childBottlesVintages.addAll(
//              repository.getWineWithBottles(wine.idWine).bottles.map { bottle -> bottle.vintage }
//            )
//            wine
//        }
}