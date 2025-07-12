package com.louis.app.cavity.ui.addbottle.viewmodel

import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.repository.FriendRepository
import com.louis.app.cavity.domain.repository.HistoryRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.BottleSize
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.addbottle.adapter.PickableFriend
import com.louis.app.cavity.util.combine
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlin.collections.map

class OtherInfoManager(
    viewModelScope: CoroutineScope,
    private val friendRepository: FriendRepository,
    private val historyRepository: HistoryRepository,
    editedBottle: Bottle?
) {
    private var pdfPath: String = ""

    val hasPdf: Boolean
        get() = pdfPath.isNotBlank()

    var partialBottle: Step4Bottle? = null

    private val friendFilterQuery = MutableLiveData("")
    private val sortFriendsByPreference = MutableLiveData(true)

    private val _selectedFriends = MutableLiveData<List<Friend>>(emptyList())
    val selectedFriends: LiveData<List<Friend>>
        get() = _selectedFriends

    /*val pickableFriends = getAllFriends().combine(selectedFriends) { friends, selectedFriends ->
        friends.map { PickableFriend(it, it in selectedFriends) }
    }*/

    init {
        editedBottle?.let {
            setPdfPath(it.pdfPath)
        }

        if (editedBottle != null) {
            viewModelScope.launch(IO) {
                val replenishment =
                    historyRepository.getReplenishmentForBottleNotPagedNotLive(editedBottle.id)

                val givenBy = replenishment?.friends ?: mutableListOf()
                _selectedFriends.postValue(givenBy.toMutableList())
            }
        }
    }

    fun setPdfPath(path: String) {
        pdfPath = path
    }

    fun setSelectedFriends(friends: List<Friend>) {
        _selectedFriends.value = friends
    }

    fun updateFriendStatus(friend: PickableFriend) {
        if (friend.checked) {
            _selectedFriends.value = _selectedFriends.value!! + friend.friend
        } else {
            _selectedFriends.value = _selectedFriends.value!! - friend.friend
        }
    }

    fun toggleSortFriendsByPreference() {
        sortFriendsByPreference.value = !(sortFriendsByPreference.value ?: false)
    }

    fun setFriendFilterQuery(query: String) {
        friendFilterQuery.value = query
    }

    fun submitOtherInfo(
        otherInfo: String,
        @IdRes checkedSize: Int,
        addToFavorite: Boolean,
        friendIds: List<Long>
    ) {
        val size = when (checkedSize) {
            R.id.rbSlim -> BottleSize.SLIM
            R.id.rbSmall -> BottleSize.SMALL
            R.id.rbNormal -> BottleSize.NORMAL
            else /* R.id.rbMagnum */ -> BottleSize.MAGNUM
        }

        partialBottle = Step4Bottle(otherInfo, size, addToFavorite.toInt(), pdfPath, friendIds)
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

    // TODO: ONGOING -> faire le switchmap selon le tri voulu
    /*fun getAllFriends(): LiveData<List<Friend>> {
        val result = MediatorLiveData<List<Friend>>()

        val currentSort = sortFriendsByPreference
        val currentFilter = friendFilterQuery

        val updateSource = {
            val sortByPref = currentSort.value ?: false
            val query = currentFilter.value.orEmpty()

            val sourceLiveData: LiveData<List<Friend>> = if (sortByPref) {
                historyRepository.getFriendSortedByFrequence()
            } else {
                friendRepository.getAllFriends()
            }

            result.removeSource(sourceLiveData)

            result.addSource(sourceLiveData) { list ->
                result.value = if (query.isBlank()) {
                    list
                } else {
                    list.filter { it.name.contains(query, ignoreCase = true) }
                }
            }
        }

        result.addSource(currentSort) { updateSource() }
        result.addSource(currentFilter) { updateSource() }

        return result
    }*/

    data class Step4Bottle(
        val otherInfo: String,
        val size: BottleSize,
        val isFavorite: Int,
        val pdfPath: String,
        val giftedBy: List<Long>
    )
}
