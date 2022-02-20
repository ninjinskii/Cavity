package com.louis.app.cavity.ui.history

import android.app.Application
import androidx.annotation.IdRes
import androidx.lifecycle.*
import androidx.paging.*
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.db.dao.BoundedHistoryEntry
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    val repository = WineRepository.getInstance(app)

    // Reuse when find a way to jump scroll into paged list
    private val _scrollTo = MutableLiveData<Event<Int>>()
    val scrollTo: LiveData<Event<Int>>
        get() = _scrollTo

    private val _selectedEntry = MutableLiveData<BoundedHistoryEntry?>(null)
    val selectedEntry: LiveData<BoundedHistoryEntry?>
        get() = _selectedEntry

    private val _filter = MutableLiveData<HistoryFilter>(HistoryFilter.NoFilter)
    val filter: LiveData<HistoryFilter>
        get() = _filter

    private val _showDatePicker = MutableLiveData<Event<Long>>(null)
    val showDatePicker: LiveData<Event<Long>>
        get() = _showDatePicker

    val entries: LiveData<PagingData<HistoryUiModel>> = filter.switchMap {
        Pager(PagingConfig(pageSize = 50, prefetchDistance = 20, enablePlaceholders = true)) {
            getDataSource(it)
        }.liveData.map { pagingData ->
            pagingData
                .map { HistoryUiModel.EntryModel(it) }
                .insertSeparators { before, after ->
                    if (shouldSeparate(before, after))
                        HistoryUiModel.HeaderModel(after?.model?.historyEntry?.date ?: 0L)
                    else null
                }
        }.cachedIn(viewModelScope)
    }

    fun start(bottleId: Long) {
        if (bottleId != -1L) {
            setFilter(HistoryFilter.BottleFilter(bottleId))
        }
    }

    // Reuse when find a way to jump scroll into paged list
    fun requestScrollToDate(timestamp: Long) {
        viewModelScope.launch(IO) {
            val entries = repository.getAllEntriesNotPagedNotLive()
            val offset = 1

            withContext(Default) {
                var headerCount = 0
                var currentDay = -1L
                var dateFounded = false

                for ((position, entry) in entries.withIndex()) {
                    val day = DateFormatter.roundToDay(entry.date)

                    if (day != currentDay) {
                        currentDay = day
                        headerCount++
                    }

                    if (day <= timestamp) {
                        dateFounded = true
                        _scrollTo.postOnce(position + headerCount - offset)
                        break
                    }

                }

                if (!dateFounded) {
                    _scrollTo.postOnce(entries.size + headerCount)
                }
            }
        }
    }

    fun requestDatePicker() {
        viewModelScope.launch(IO) {
            val oldestEntryDate = repository.getOldestEntryDate()
            _showDatePicker.postOnce(oldestEntryDate)
        }
    }

    fun setFilter(filter: HistoryFilter) {
        _selectedEntry.postValue(null)
        _filter.postValue(filter)
    }

    fun setSelectedHistoryEntry(entry: BoundedHistoryEntry?) {
        _selectedEntry.postValue(entry)
    }

    fun updateHistoryEntry(entry: HistoryEntry) {
        viewModelScope.launch(IO) {
            repository.updateEntry(entry)
        }
    }

    private fun shouldSeparate(
        before: HistoryUiModel.EntryModel?,
        after: HistoryUiModel?
    ): Boolean {
        if (before == null && after == null) {
            return false
        }

        return if (after is HistoryUiModel.EntryModel?) {
            val beforeTimestamp =
                DateFormatter.roundToDay(before?.model?.historyEntry?.date ?: return true)
            val afterTimestamp =
                DateFormatter.roundToDay(after?.model?.historyEntry?.date ?: return false)

            beforeTimestamp != afterTimestamp
        } else false
    }

    private fun getDataSource(filter: HistoryFilter):
        PagingSource<Int, BoundedHistoryEntry> {
        return when (filter) {
            is HistoryFilter.TypeFilter -> when (filter.chipId) {
                R.id.chipReplenishments -> repository.getEntriesByType(1, 3)
                R.id.chipComsumptions -> repository.getEntriesByType(0, 2)
                R.id.chipTastings -> repository.getEntriesByType(4, 4)
                R.id.chipGiftedTo -> repository.getEntriesByType(2, 2)
                R.id.chipGiftedBy -> repository.getEntriesByType(3, 3)
                R.id.chipFavorites -> repository.getFavoriteEntries()
                else -> repository.getAllEntries()
            }
            is HistoryFilter.BottleFilter -> repository.getEntriesForBottle(filter.bottleId)
            is HistoryFilter.DateFilter -> repository.getEntriesForDate(filter.date)
            is HistoryFilter.NoFilter -> repository.getAllEntries()
        }
    }

}

sealed class HistoryFilter {
    class DateFilter(val date: Long) : HistoryFilter() /* Workaround for paging fast scroll */
    class TypeFilter(@IdRes val chipId: Int) : HistoryFilter()
    class BottleFilter(val bottleId: Long) : HistoryFilter()
    object NoFilter : HistoryFilter()
}
