package com.louis.app.cavity.domain.repository

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val Application.dataStore by preferencesDataStore(name = "settings")

class PrefsRepository private constructor(app: Application) {
    companion object {
        const val MIN_TEMPLATE_SCALE = 0.4f

        private val PREF_SKEW_BOTTLE =
            booleanPreferencesKey("com.louis.app.cavity.PREF_SKEW_BOTTLE")

        private val PREF_DEFAULT_CURRENCY =
            stringPreferencesKey("com.louis.app.cavity.PREF_DEFAULT_CURRENCY")

        private val PREF_TEMPLATE_SIZE =
            floatPreferencesKey("com.louis.app.cavity.PREF_TEMPLATE_SIZE")

        private val PREF_API_TOKEN =
            stringPreferencesKey("com.louis.app.cavity.PREF_API_TOKEN")

        private val PREF_LAST_LOGIN =
            stringPreferencesKey("com.louis.app.cavity.PREF_LAST_LOGIN")

        private val PREF_AUTO_BACKUP =
            booleanPreferencesKey("com.louis.app.cavity.PREF_AUTO_BACKUP")

        private val PREF_ERROR_REPORTING_CONSENT =
            booleanPreferencesKey("com.louis.app.cavity.PREF_ERROR_REPORTING_CONSENT")

        private val PREF_PREVENT_SCREENSHOTS =
            booleanPreferencesKey("com.louis.app.cavity.PREF_PREVENT_SCREENSHOTS")

        private val PREF_ENABLE_STORAGE_LOCATION =
            booleanPreferencesKey("com.louis.app.cavity.PREF_ENABLE_STORAGE_LOCATION")

        private const val DEFAULT_CURRENCY = "€"
        private const val MAX_TEMPLATE_SCALE = 1.4f

        @Volatile
        var instance: PrefsRepository? = null

        fun getInstance(app: Application) =
            instance ?: synchronized(this) {
                instance ?: PrefsRepository(app).also { instance = it }
            }
    }

    private val dataStore = app.dataStore

    val skewBottle: Flow<Boolean> =
        dataStore.data.map { it[PREF_SKEW_BOTTLE] ?: false }

    val defaultCurrency: Flow<String> =
        dataStore.data.map { it[PREF_DEFAULT_CURRENCY] ?: DEFAULT_CURRENCY }

    val templateSize: Flow<Float> =
        dataStore.data.map {
            (it[PREF_TEMPLATE_SIZE] ?: 0.9f)
                .coerceIn(MIN_TEMPLATE_SCALE, MAX_TEMPLATE_SCALE)
        }

    val apiToken: Flow<String> =
        dataStore.data.map { it[PREF_API_TOKEN] ?: "" }

    val lastLogin: Flow<String> =
        dataStore.data.map { it[PREF_LAST_LOGIN] ?: "" }

    val autoBackup: Flow<Boolean> =
        dataStore.data.map { it[PREF_AUTO_BACKUP] ?: false }

    val errorReportingConsent: Flow<Boolean> =
        dataStore.data.map { it[PREF_ERROR_REPORTING_CONSENT] ?: false }

    val preventScreenshots: Flow<Boolean> =
        dataStore.data.map { it[PREF_PREVENT_SCREENSHOTS] ?: false }

    val enableBottleStorageLocation: Flow<Boolean> =
        dataStore.data.map { it[PREF_ENABLE_STORAGE_LOCATION] ?: false }

    suspend fun setSkewBottle(skew: Boolean) {
        dataStore.edit { it[PREF_SKEW_BOTTLE] = skew }
    }

    suspend fun setDefaultCurrency(currency: String) {
        dataStore.edit { it[PREF_DEFAULT_CURRENCY] = currency }
    }

    suspend fun setTemplateSize(templateSize: Float) {
        dataStore.edit {
            it[PREF_TEMPLATE_SIZE] =
                templateSize.coerceIn(MIN_TEMPLATE_SCALE, MAX_TEMPLATE_SCALE)
        }
    }

    suspend fun setApiToken(token: String) {
        dataStore.edit { it[PREF_API_TOKEN] = token }
    }

    suspend fun setLastLogin(email: String) {
        dataStore.edit { it[PREF_LAST_LOGIN] = email }
    }

    suspend fun setAutoBackup(autoBackup: Boolean) {
        dataStore.edit { it[PREF_AUTO_BACKUP] = autoBackup }
    }

    suspend fun setErrorReportingConsent(consent: Boolean) {
        dataStore.edit { it[PREF_ERROR_REPORTING_CONSENT] = consent }
    }

    suspend fun setPreventScreenshots(preventScreenshots: Boolean) {
        dataStore.edit { it[PREF_PREVENT_SCREENSHOTS] = preventScreenshots }
    }

    suspend fun setEnableBottleStorageLocation(enableStorageLocation: Boolean) {
        dataStore.edit { it[PREF_ENABLE_STORAGE_LOCATION] = enableStorageLocation }
    }
}
