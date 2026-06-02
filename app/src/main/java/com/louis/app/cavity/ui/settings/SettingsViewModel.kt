package com.louis.app.cavity.ui.settings

import android.app.Application
import androidx.annotation.StringRes
import com.louis.app.cavity.domain.repository.PrefsRepository
import com.louis.app.cavity.ui.BaseViewModel

sealed interface SettingsEvent {
    data class UserFeedback(@StringRes val resId: Int) : SettingsEvent
    data class WindowFocusChanged(val hasFocus: Boolean) : SettingsEvent
}

data class SettingsUiState(val isLoading: Boolean = false)

class SettingsViewModel(app: Application) : BaseViewModel<SettingsUiState, SettingsEvent>(app, SettingsUiState()) {
    private val prefsRepository = PrefsRepository.getInstance(app)

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

    fun setErrorReportingConsent(consent: Boolean) {
        prefsRepository.setErrorReportingConsent(consent)
    }

    fun setPreventScrenshots(preventScreenshots: Boolean) {
        prefsRepository.setPreventScreenshots(preventScreenshots)
    }

    fun setEnableBottleStorageLocation(enableStorageLocation: Boolean) {
        prefsRepository.setEnableBottleStorageLocation(enableStorageLocation)
    }

    fun getSkewBottle() = prefsRepository.getSkewBottle()

    fun getDefaultCurrency() = prefsRepository.getDefaultCurrency()

    fun getTemplateSize() = prefsRepository.getTemplateSize()

    fun getAutoBackup() = prefsRepository.getAutoBackup()

    fun getErrorReportingConsent() = prefsRepository.getErrorReportingConsent()

    fun getPreventScreenshots() = prefsRepository.getPreventScreenshots()

    fun getEnableBottleStorageLocation() = prefsRepository.getEnableBottleStorageLocation()

    fun notifyWindowFocusChanged(hasFocus: Boolean) {
        emitEvent(SettingsEvent.WindowFocusChanged(hasFocus))
    }
}
