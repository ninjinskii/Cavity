package com.louis.app.cavity.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.PrefsRepository
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.postOnce
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = WineRepository.getInstance(app)
    private val prefsRepository = PrefsRepository.getInstance(app)

    private val _userFeedback = MutableLiveData<Event<Int>>()
    val userFeedback: LiveData<Event<Int>>
        get() = _userFeedback

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    fun setSkewBottle(skew: Boolean) {
        prefsRepository.setSkewBottle(skew)
    }

    fun setDefaultCurrency(currency: String) {
        prefsRepository.setDefaultCurrency(currency)
    }

    fun setTemplateSize(templateSize: Float) {
        prefsRepository.setTemplateSize(templateSize)
    }

    fun setAutoBackup(autoBackup: Boolean) {
        prefsRepository.setAutoBackup(autoBackup)
    }

    fun getSkewBottle() = prefsRepository.getSkewBottle()

    fun getDefaultCurrency() = prefsRepository.getDefaultCurrency()

    fun getTemplateSize() = prefsRepository.getTemplateSize()

    fun getAutoBackup() = prefsRepository.getAutoBackup()

    fun importDbFromExternalDir(externalDir: String) {
        viewModelScope.launch(IO) {
            if (!_isLoading.value!!) {
                _isLoading.postValue(true)

                try {
                    repository.importDbFromExternalDir(externalDir)
                    _userFeedback.postOnce(R.string.db_import_success)
                } catch (e: IllegalStateException) {
                    Sentry.captureException(e)
                    _userFeedback.postOnce(R.string.base_error)
                } finally {
                    _isLoading.postValue(false)
                }
            }
        }
    }
}
