package com.louis.app.cavity.domain.error

interface ErrorReporter {

    companion object {
        const val USERNAME_ERROR_TAG = "username"
    }

    fun captureException(throwable: Throwable)
    fun captureMessage(message: String)
    fun setScopeTag(tag: String, value: String)
    fun removeScopeTag(tag: String)
    fun stopEvents(): Boolean
}
