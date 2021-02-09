package com.louis.app.cavity.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.util.DateFormatter

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    val repository = WineRepository.getInstance(app)

    val entries: LiveData<PagingData<HistoryUiModel>> =
        Pager(PagingConfig(pageSize = 100, prefetchDistance = 10, enablePlaceholders = true)) {
            repository.getAllEntries()
        }.liveData.map { pagingData ->
            pagingData
                .map { HistoryUiModel.EntryModel(it) }
                .insertSeparators { before, after ->
                    if (shouldSeparate(before, after))
                        HistoryUiModel.HeaderModel(after?.model?.historyEntry?.date ?: 0L)
                    else null
                }
        }.cachedIn(viewModelScope)

    private fun shouldSeparate(
        before: HistoryUiModel.EntryModel?,
        after: HistoryUiModel?
    ): Boolean {
        return if (after is HistoryUiModel.EntryModel?) {
            val beforeTimestamp =
                DateFormatter.roundToDay(before?.model?.historyEntry?.date ?: return false)
            val afterTimestamp =
                DateFormatter.roundToDay(after?.model?.historyEntry?.date ?: return false)

            beforeTimestamp != afterTimestamp
        } else false
    }
}
