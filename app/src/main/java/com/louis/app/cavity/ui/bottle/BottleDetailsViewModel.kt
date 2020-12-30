package com.louis.app.cavity.ui.bottle

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class BottleDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)

    private val _pdfEvent = MutableLiveData<Event<Uri>>()
    val pdfEvent: LiveData<Event<Uri>>
        get() = _pdfEvent

    private val _imageEvent = MutableLiveData<Event<Uri>>()
    val imageEvent: LiveData<Event<Uri>>
        get() = _imageEvent

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    fun getBottleById(bottleId: Long) = repository.getBottleById(bottleId)

    fun getQGrapesForBottle(bottleId: Long) = repository.getQGrapesAndGrapeForBottle(bottleId)

    fun getFReviewForBottle(bottleId: Long) = repository.getFReviewAndReviewForBottle(bottleId)

    fun preparePdf(bottleId: Long) {
        viewModelScope.launch(IO) {
            val bottle = repository.getBottleByIdNotLive(bottleId)
            val path = bottle.pdfPath

            if (path.isNotBlank()) {
                _pdfEvent.postOnce(Uri.parse(path))
            } else {
                _userFeedback.postOnce(R.string.no_pdf)
            }
        }
    }

    fun prepareImage(wineId: Long) {
        viewModelScope.launch(IO) {
            val wine = repository.getWineByIdNotLive(wineId)
            val path = wine.imgPath

            if (path.isNotBlank()) {
                _imageEvent.postOnce(Uri.parse(path))
            }
        }
    }
}
