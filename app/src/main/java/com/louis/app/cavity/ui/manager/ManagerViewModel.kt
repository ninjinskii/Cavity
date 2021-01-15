package com.louis.app.cavity.ui.manager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.WineRepository
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

    fun swapCounties(county1Id: Long, pos1: Int, county2Id: Long, pos2: Int) {
        viewModelScope.launch(IO) {
            repository.swapCounties(county1Id, pos1, county2Id, pos2)
        }
    }
}