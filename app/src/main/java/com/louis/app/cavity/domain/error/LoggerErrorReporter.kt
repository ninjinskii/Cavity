package com.louis.app.cavity.domain.error

import android.util.Log

class LoggerErrorReporter : ErrorReporter {

    override fun stopEvents() = false

    override fun captureException(throwable: Throwable) {
        Log.e("___ERROR_REPORTER___", "", throwable)
    }

    override fun captureMessage(message: String) {
        Log.v("___ERROR_REPORTER___", message)
    }

    override fun setScopeTag(tag: String, value: String) {
        Log.v("___ERROR_REPORTER___", "########### SCOPE $value ###########")
    }

    override fun removeScopeTag(tag: String) {
        Log.v("___ERROR_REPORTER___", "########### END ###########")
    }
}
