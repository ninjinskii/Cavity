package com.louis.app.cavity.ui.addbottle

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.ui.addbottle.viewmodel.DateManager
import com.louis.app.cavity.ui.addbottle.viewmodel.GrapeManager
import com.louis.app.cavity.ui.addbottle.viewmodel.ReviewManager
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AddBottleViewModel(app: Application) : AndroidViewModel(app) {
    // lazy ?
    lateinit var dateManager: DateManager
    lateinit var grapeManager: GrapeManager
    lateinit var reviewManager: ReviewManager
//    private lateinit var otherInfoManager: OtherInfoManager

    private val repository = WineRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private val _editedBottle = MutableLiveData<Bottle>()
    val editedBottle: LiveData<Bottle>
        get() = _editedBottle

    private var wineId = 0L

    fun start(wineId: Long, bottleId: Long) {
        this.wineId = wineId

        viewModelScope.launch(IO) {
            val bottle: Bottle? = repository.getBottleByIdNotLive(bottleId)
            _editedBottle.postValue(bottle)

            dateManager = DateManager(viewModelScope, repository, bottle) { postFeedback(it) }
            grapeManager = GrapeManager(viewModelScope, repository, bottle) { postFeedback(it) }
            reviewManager = ReviewManager(viewModelScope, repository, bottle) { postFeedback(it) }
        }
    }

    fun insertToDb() {
        // récupérer objets dans les managers
        // tout insérer en db
    }

    private fun postFeedback(@StringRes stringRes: Int) {
        _userFeedback.postOnce(stringRes)
    }
}
