package com.louis.app.cavity.ui.settings

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.domain.repository.PrefsRepository
import com.louis.app.cavity.ui.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SettingsEvent {
    data class UserFeedback(@param:StringRes val resId: Int) : SettingsEvent
    data class WindowFocusChanged(val hasFocus: Boolean) : SettingsEvent
}

data class SettingsState(
    val skewBottle: Boolean = false,
    val defaultCurrency: String = "€",
    val templateSize: Float = 1f
)

class SettingsViewModel(
    app: Application,
    private val prefsRepository: PrefsRepository
) :
    BaseViewModel<SettingsState, SettingsEvent>(app, SettingsState()) {

    val skewBottle = prefsRepository.skewBottle
    val defaultCurrency = prefsRepository.defaultCurrency
    val templateSize = prefsRepository.templateSize
    val autoBackup = prefsRepository.autoBackup
    val errorReportingConsent = prefsRepository.errorReportingConsent
    val preventScreenshots = prefsRepository.preventScreenshots
    val enableBottleStorageLocation = prefsRepository.enableBottleStorageLocation

    /**
     * An error reporting version that gets the firs value immediatly, avoiding the nned for a
     * coroutine when getting instances of ErrorReporter
     */
    val errorReportingConsentSync = errorReportingConsent
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )

    fun setSkewBottle(skew: Boolean) {
        viewModelScope.launch {
            prefsRepository.setSkewBottle(skew)
        }
    }

    fun setDefaultCurrency(currency: String) {
        viewModelScope.launch {
            prefsRepository.setDefaultCurrency(currency)
        }
    }

    fun setTemplateSize(templateSize: Float) {
        viewModelScope.launch {
            prefsRepository.setTemplateSize(templateSize)
        }
    }

    fun setAutoBackup(autoBackup: Boolean) {
        viewModelScope.launch {
            prefsRepository.setAutoBackup(autoBackup)
        }
    }

    fun setErrorReportingConsent(consent: Boolean) {
        viewModelScope.launch {
            prefsRepository.setErrorReportingConsent(consent)
        }
    }

    fun setPreventScreenshots(preventScreenshots: Boolean) {
        viewModelScope.launch {
            prefsRepository.setPreventScreenshots(preventScreenshots)

        }
    }

    fun setEnableBottleStorageLocation(enableStorageLocation: Boolean) {
        viewModelScope.launch {
            prefsRepository.setEnableBottleStorageLocation(enableStorageLocation)
        }
    }

    fun notifyWindowFocusChanged(hasFocus: Boolean) {
        emitEvent(SettingsEvent.WindowFocusChanged(hasFocus))
    }
}
