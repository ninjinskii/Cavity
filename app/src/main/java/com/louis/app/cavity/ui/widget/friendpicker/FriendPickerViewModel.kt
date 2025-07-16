package com.louis.app.cavity.ui.widget.friendpicker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.domain.repository.FriendRepository
import com.louis.app.cavity.domain.repository.HistoryRepository
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.addbottle.adapter.PickableFriend
import com.louis.app.cavity.util.combine
import com.louis.app.cavity.util.minusAssign
import com.louis.app.cavity.util.plusAssign
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class FriendPickerViewModel(app: Application) : AndroidViewModel(app) {
    private val friendRepository = FriendRepository.getInstance(app)
    private val historyRepository = HistoryRepository.getInstance(app)

    private val friendFilterQuery = MutableLiveData("")
    private val sortFriendsByPreference = MutableLiveData(true)

    private val _selectedFriends = MutableLiveData<MutableList<Friend>>(mutableListOf())
    val selectedFriends: LiveData<MutableList<Friend>>
        get() = _selectedFriends

    private var started = false

    fun fetchFriendsFromEditedBottleId(bottleId: Long) {
        // Avoid reloading empty friends when adding new bottle an initiate configuration change
        if (started) {
            return
        }

        started = true

        viewModelScope.launch(IO) {
            val replenishment =
                historyRepository.getReplenishmentForBottleNotPagedNotLive(bottleId)

            val givenBy = replenishment?.friends ?: mutableListOf()
            _selectedFriends.postValue(givenBy.toMutableList())
        }
    }

    fun updateFriendStatus(pickableFriend: PickableFriend) {
        _selectedFriends.let {
            if (pickableFriend.checked) it += pickableFriend.friend else it -= pickableFriend.friend
        }
    }

    fun toggleSortFriendsByPreference() {
        sortFriendsByPreference.value = !(sortFriendsByPreference.value ?: false)
    }

    fun setFriendFilterQuery(query: String) {
        friendFilterQuery.value = query
    }

    fun setSelectedFriends(selectedFiends: List<Friend>) {
        _selectedFriends.value = selectedFiends.toMutableList()
    }

    fun a() = getAllFriends().combine(_selectedFriends) { friends, selected ->
        friends.map { PickableFriend(it, it in selected) }
    }

    fun getAllFriends(): LiveData<List<Friend>> {
        return sortFriendsByPreference.combine(friendFilterQuery) { sortByPref, query ->
            Pair(sortByPref, query)
        }.switchMap { (sortByPref, query) ->
            val source: LiveData<List<Friend>> = if (sortByPref) {
                historyRepository.getFriendSortedByFrequence()
            } else {
                friendRepository.getAllFriends()
            }

            source.map { list ->
                if (query.isBlank()) list
                else list.filter { it.name.contains(query, ignoreCase = true) }
            }
        }
    }

    fun getSelectedFriendsIds() = _selectedFriends.value?.map { it.id } ?: emptyList()
}
