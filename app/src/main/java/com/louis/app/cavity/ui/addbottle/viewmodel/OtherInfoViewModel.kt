package com.louis.app.cavity.ui.addbottle.viewmodel

import android.database.sqlite.SQLiteConstraintException
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class OtherInfoManager(
    private val viewModelScope: CoroutineScope,
    private val repository: WineRepository,
    editedBottle: Bottle?,
    private val postFeedback: (Int) -> Unit
) {
    private var pdfPath: String = ""

    val hasPdf: Boolean
        get() = pdfPath.isNotBlank()

    var partialBottle: Step4Bottle? = null

    init {
        editedBottle?.let {
            setPdfPath(it.pdfPath)
        }
    }

    fun setPdfPath(path: String) {
        pdfPath = path
    }

    fun submitOtherInfo(otherInfo: String, addToFavorite: Boolean, friendId: Long?) {
        partialBottle = Step4Bottle(otherInfo, addToFavorite.toInt(), pdfPath, friendId)
    }

//    fun saveBottle(otherInfo: String, addToFavorite: Boolean, friendId: Long?) {
//        viewModelScope.launch(IO) {
//            val step1 = repository.getBottleByIdNotLive(bottleId)
//            val bottle = mergeStep1Bottle(step1, addToFavorite, otherInfo)
//
//            repository.updateBottle(bottle)
//            addHistoryEntry(bottle.buyDate, friendId)
//
//            _updatedBottle.postValue(null)
//            _bottleUpdatedEvent.postOnce(R.string.bottle_added)
//        }
//    }

    fun getAllFriends() = repository.getAllFriends()

    fun insertFriend(nameLastName: String) {
        viewModelScope.launch(IO) {
            try {
                repository.insertFriend(Friend(0, nameLastName, ""))
                postFeedback(R.string.friend_added)
            } catch (e: IllegalArgumentException) {
                postFeedback(R.string.input_error)
            } catch (e: SQLiteConstraintException) {
                postFeedback(R.string.friend_already_exists)
            }
        }
    }

//    private suspend fun addHistoryEntry(buyDate: Long, friendId: Long?) {
//        if (friendId == null) {
//            val typeReplenishment = 1
//            val historyEntry =
//                HistoryEntry(0, buyDate, bottleId, null, "", typeReplenishment, 0)
//            repository.insertHistoryEntry(historyEntry)
//        } else {
//            val typeGiftedBy = 3
//            val historyEntry =
//                HistoryEntry(0, buyDate, bottleId, null, "", typeGiftedBy, 0)
//            repository.declareGiftedBottle(historyEntry, friendId)
//        }
//    }

    data class Step4Bottle(
        val otherInfo: String,
        val isFavorite: Int,
        val pdfPath: String,
        val giftedBy: Long?
    )
}
