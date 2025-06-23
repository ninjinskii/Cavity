package com.louis.app.cavity.ui.addbottle.viewmodel

import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.repository.FriendRepository
import com.louis.app.cavity.domain.repository.HistoryRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.BottleSize
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.addbottle.adapter.PickableFriend
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

    private val _selectedFriends = MutableLiveData<MutableList<Friend>>(mutableListOf())
    val selectedFriends: LiveData<MutableList<Friend>>
        get() = _selectedFriends

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

    fun getAllFriends() = friendRepository.getAllFriends()

    data class Step4Bottle(
        val otherInfo: String,
        val size: BottleSize,
        val isFavorite: Int,
        val pdfPath: String,
        val giftedBy: List<Long>
    )
}
