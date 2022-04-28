package com.louis.app.cavity.db

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.louis.app.cavity.R

class PrefsRepository private constructor(app: Application) {
    companion object {
        private const val PREF_SKEW_BOTTLE = "com.louis.app.cavity.PREF_SKEW_BOTTLE"
        private const val PREF_DEFAULT_CURRENCY = "com.louis.app.cavity.PREF_DEFAULT_CURRENCY"
        private const val PREF_API_TOKEN = "com.louis.app.cavity.PREF_API_TOKEN"
        private const val DEFAULT_CURRENCY = "€"

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
        PREF_SKEW_BOTTLE.put(skew)
    }

    fun setDefaultCurrency(currency: String) {
        PREF_DEFAULT_CURRENCY.put(currency)
    }

    fun setApiToken(token: String) {
        PREF_API_TOKEN.put(token)
    }

    fun getSkewBottle() = PREF_SKEW_BOTTLE.getBoolean()

    fun getDefaultCurrency() = PREF_DEFAULT_CURRENCY.getString() ?: DEFAULT_CURRENCY

    fun getApiToken() = PREF_API_TOKEN.getString() ?: ""

    private fun String.put(string: String) {
        editor.putString(this, string)
        editor.commit()
    }

    private fun String.put(boolean: Boolean) {
        editor.putBoolean(this, boolean)
        editor.commit()
    }

    private fun String.getString(): String? = pref.getString(this, null)

    private fun String.getBoolean() = pref.getBoolean(this, true)
}
