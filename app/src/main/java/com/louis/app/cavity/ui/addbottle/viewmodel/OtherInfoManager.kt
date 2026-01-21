package com.louis.app.cavity.ui.addbottle.viewmodel

import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.repository.TagRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.BottleSize
import com.louis.app.cavity.model.Tag
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine

class OtherInfoManager(
    private val editedBottle: Bottle?,
    viewModelScope: CoroutineScope,
    private val tagRepository: TagRepository
) {

    private val _tagDialogEvent = MutableLiveData<Event<List<Tag>>>()
    val tagDialogEvent: LiveData<Event<List<Tag>>>
        get() = _tagDialogEvent

    private var pdfPath: String = ""

    val hasPdf: Boolean
        get() = pdfPath.isNotBlank()

    var partialBottle: Step4Bottle? = null

    init {
        editedBottle?.let {
            setPdfPath(it.pdfPath)
        }
    }

    fun getAllTags() = tagRepository.getAllTags().asLiveData()

    fun getAllTagsWithSelection() = combine(
        tagRepository.getAllTags(),
        tagRepository.getTagIdsForBottle(editedBottle?.id ?: -1)
    ) { tags, selectedIds ->
        tags.also { tags -> tags.forEach { it.selected = it.id in selectedIds } }
    }
        .asLiveData()

    fun setPdfPath(path: String) {
        pdfPath = path
    }

    fun submitOtherInfo(
        storageLocation: String,
        alcohol: Double?,
        otherInfo: String,
        @IdRes checkedSize: Int,
        addToFavorite: Boolean,
        friendIds: List<Long>,
        tags: List<Tag>
    ) {
        val size = when (checkedSize) {
            R.id.rbSlim -> BottleSize.SLIM
            R.id.rbSmall -> BottleSize.SMALL
            R.id.rbNormal -> BottleSize.NORMAL
            else /* R.id.rbMagnum */ -> BottleSize.MAGNUM
        }

        partialBottle = Step4Bottle(
            storageLocation,
            alcohol,
            otherInfo,
            size,
            addToFavorite.toInt(),
            pdfPath,
            friendIds,
            tags
        )
    }

    data class Step4Bottle(
        val storageLocation: String,
        val alcohol: Double?,
        val otherInfo: String,
        val size: BottleSize,
        val isFavorite: Int,
        val pdfPath: String,
        val giftedBy: List<Long>,
        val tags: List<Tag>
    )
}
