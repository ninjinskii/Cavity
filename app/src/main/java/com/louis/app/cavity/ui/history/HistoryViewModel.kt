package com.louis.app.cavity.ui.history

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.*
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    val repository = WineRepository.getInstance(app)

    private val _scrollTo = MutableLiveData<Event<Int>>()
    val scrollTo: LiveData<Event<Int>>
        get() = _scrollTo

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

    fun requestScrollToDate(timestamp: Long) {
        viewModelScope.launch(IO) {
            val entries = repository.getAllEntriesNotPagedNotLive()
            val offset = 1

            withContext(Default) {
                var headerCount = 0
                var currentDay = -1L

                for ((position, entry) in entries.withIndex()) {
                    val day = DateFormatter.roundToDay(entry.date)

                    if (day != currentDay) {
                        currentDay = day
                        headerCount++
                    }

                    if (entry.date <= DateFormatter.roundToDay(timestamp)) {
                        _scrollTo.postOnce(position + headerCount - offset)
                        break
                    }

                    // No date found, scroll to bottom
                    _scrollTo.postOnce(position + headerCount)
                }
            }
        }
    }

    private fun shouldSeparate(
        before: HistoryUiModel.EntryModel?,
        after: HistoryUiModel?
    ): Boolean {
        return if (after is HistoryUiModel.EntryModel?) {
            val beforeTimestamp =
                DateFormatter.roundToDay(before?.model?.historyEntry?.date ?: return true)
            val afterTimestamp =
                DateFormatter.roundToDay(after?.model?.historyEntry?.date ?: return false)

            beforeTimestamp != afterTimestamp
        } else false
    }
}
