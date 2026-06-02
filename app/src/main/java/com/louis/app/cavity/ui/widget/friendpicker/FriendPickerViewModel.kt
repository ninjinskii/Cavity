package com.louis.app.cavity.ui.widget.friendpicker

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.domain.repository.FriendRepository
import com.louis.app.cavity.domain.repository.HistoryRepository
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.BaseViewModel
import com.louis.app.cavity.ui.addbottle.adapter.PickableFriend
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class FriendPickerUiState(
    val pickableFriends: List<PickableFriend> = emptyList(),
    val selectedFriends: List<Friend> = emptyList()
)

class FriendPickerViewModel(app: Application) : BaseViewModel<FriendPickerUiState, Nothing>(app, FriendPickerUiState()) {
    private val friendRepository = FriendRepository.getInstance(app)
    private val historyRepository = HistoryRepository.getInstance(app)

    private val _friendFilterQuery = MutableStateFlow("")
    private val _sortFriendsByPreference = MutableStateFlow(true)

    private val _selectedFriends = MutableStateFlow<List<Friend>>(emptyList())
    val selectedFriends: StateFlow<List<Friend>> = _selectedFriends.asStateFlow()

    private var started = false

    private val sortedFilteredFriends: Flow<List<Friend>> =
        combine(_sortFriendsByPreference, _friendFilterQuery) { sortByPref, query ->
            sortByPref to query
        }.flatMapLatest { (sortByPref, query) ->
            val source = if (sortByPref) historyRepository.getFriendSortedByFrequence()
                         else friendRepository.getAllFriends()
            source.map { list ->
                if (query.isBlank()) list
                else list.filter { it.name.contains(query, ignoreCase = true) }
            }
        }

    init {
        combine(sortedFilteredFriends, _selectedFriends) { friends, selected ->
            FriendPickerUiState(
                pickableFriends = friends.map { PickableFriend(it, it in selected) },
                selectedFriends = selected
            )
        }
            .onEach { viewState = it }
            .launchIn(viewModelScope)
    }

    fun fetchFriendsFromEditedBottleId(bottleId: Long) {
        if (started) return
        started = true

        viewModelScope.launch(IO) {
            val replenishment = historyRepository.getReplenishmentForBottleNotPagedNotLive(bottleId)
            _selectedFriends.value = replenishment?.friends ?: emptyList()
        }
    }

    fun updateFriendStatus(pickableFriend: PickableFriend) {
        val current = _selectedFriends.value.toMutableList()
        if (pickableFriend.checked) current.add(pickableFriend.friend)
        else current.remove(pickableFriend.friend)
        _selectedFriends.value = current
    }

    fun toggleSortFriendsByPreference() {
        _sortFriendsByPreference.value = !_sortFriendsByPreference.value
    }

    fun setFriendFilterQuery(query: String) {
        _friendFilterQuery.value = query
    }

    fun getAllFriends(): Flow<List<Friend>> = sortedFilteredFriends

    fun getPickableFriends(): Flow<List<PickableFriend>> = state.map { it.pickableFriends }

    fun getSortByPreference() = _sortFriendsByPreference.value

    fun getSelectedFriendsIds() = _selectedFriends.value.map { it.id }

    fun getSelectedFriends() = _selectedFriends.value
}
