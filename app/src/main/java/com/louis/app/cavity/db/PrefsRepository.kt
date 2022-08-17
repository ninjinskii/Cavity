package com.louis.app.cavity.db

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.louis.app.cavity.R

class PrefsRepository private constructor(app: Application) {
    companion object {
        private const val PREF_SKEW_BOTTLE = "com.louis.app.cavity.PREF_SKEW_BOTTLE"
        private const val PREF_DEFAULT_CURRENCY = "com.louis.app.cavity.PREF_DEFAULT_CURRENCY"
        private const val PREF_TEMPLATE_SIZE = "com.louis.app.cavity.PREF_TEMPLATE_SIZE"
        private const val PREF_API_TOKEN = "com.louis.app.cavity.PREF_API_TOKEN"
        private const val PREF_LAST_LOGIN = "com.louis.app.cavity.PREF_LAST_LOGIN"
        private const val DEFAULT_CURRENCY = "€"
        private const val MAX_TEMPLATE_SCALE = 1.4f
        private const val MIN_TEMPLATE_SCALE = 0.4f

        @Volatile
        var instance: PrefsRepository? = null

        fun getInstance(app: Application) =
            instance ?: synchronized(this) {
                instance ?: PrefsRepository(app).also { instance = it }
            }
    }

    private val prefKey = app.getString(R.string.tag_shared_prefs)
    private val pref: SharedPreferences = app.getSharedPreferences(prefKey, Context.MODE_PRIVATE)
    private val editor = pref.edit()

    fun setSkewBottle(skew: Boolean) {
        PREF_SKEW_BOTTLE put skew
    }

    fun setDefaultCurrency(currency: String) {
        PREF_DEFAULT_CURRENCY put currency
    }

    fun setTemplateSize(templateSize: Float) {
        PREF_TEMPLATE_SIZE put templateSize.coerceIn(MIN_TEMPLATE_SCALE, MAX_TEMPLATE_SCALE)
    }

    fun setApiToken(token: String) {
        PREF_API_TOKEN put token
    }

    fun setLastLogin(email: String) {
        PREF_LAST_LOGIN put email
    }

    fun getSkewBottle() = PREF_SKEW_BOTTLE.getBoolean()

    fun getDefaultCurrency() = PREF_DEFAULT_CURRENCY.getString() ?: DEFAULT_CURRENCY

    fun getTemplateSize() =
        PREF_TEMPLATE_SIZE.getFloat().coerceIn(MIN_TEMPLATE_SCALE, MAX_TEMPLATE_SCALE)

    fun getApiToken() = PREF_API_TOKEN.getString() ?: ""

    fun getLastLogin() = PREF_LAST_LOGIN.getString() ?: ""

    private infix fun String.put(string: String) {
        editor.putString(this, string)
        editor.commit()
    }

    private infix fun String.put(boolean: Boolean) {
        editor.putBoolean(this, boolean)
        editor.commit()
    }

    private infix fun String.put(float: Float) {
        editor.putFloat(this, float)
        editor.commit()
    }

    private fun String.getString(): String? = pref.getString(this, null)

    private fun String.getBoolean() = pref.getBoolean(this, true)

    private fun String.getFloat() = pref.getFloat(this, 0.9f)
}
