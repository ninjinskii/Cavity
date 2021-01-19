package com.louis.app.cavity.ui.manager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.County
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ManagerViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    suspend fun getCountiesWithWinesNotLive() = repository.getCountiesWithWinesNotLive()

    fun getAllCounties() = repository.getAllCounties()

    fun updateCounty(county: County) {
        viewModelScope.launch(IO) {
            repository.updateCounty(county)
        }
    }

    fun deleteCounty(countyId: Long) {
        viewModelScope.launch(IO) {
            repository.deleteCounty(countyId)
        }
    }

    fun updateCounties(counties: List<County>) {
        viewModelScope.launch(IO) {
            repository.updateCounties(counties)
        }
    }
}