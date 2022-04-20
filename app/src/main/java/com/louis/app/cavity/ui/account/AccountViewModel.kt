package com.louis.app.cavity.ui.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.AccountRepository
import com.louis.app.cavity.db.WineRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AccountViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)
    private val accountRepository = AccountRepository.getInstance(app)

    fun export() {
        viewModelScope.launch(IO) {
            val counties = repository.getAllCountiesNotLive()
            accountRepository.postCounties(counties)
        }
    }
}
