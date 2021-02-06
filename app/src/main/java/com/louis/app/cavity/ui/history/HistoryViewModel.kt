package com.louis.app.cavity.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.louis.app.cavity.db.WineRepository

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    val repository = WineRepository.getInstance(app)

    val entries =
        Pager(PagingConfig(pageSize = 60, prefetchDistance = 200, enablePlaceholders = true)) {
            repository.getAllEntries()
        }
}
