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

    init {
//        viewModelScope.launch(IO) {
//            repository.resetOrder()
//        }
    }

    suspend fun getCountiesWithWinesNotLive() = repository.getCountiesWithWinesNotLive()

    suspend fun getAllCountiesNotLive() = repository.getAllCountiesNotLive()

    fun saveCountiesOrder(counties: List<County>) {

        viewModelScope.launch(IO) {
            repository.swapCounties()
        }
    }
}