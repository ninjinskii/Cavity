package com.louis.app.cavity.domain.error

class FakeErrorReporter : ErrorReporter {

    override fun stopEvents() = true

    override fun captureException(throwable: Throwable) = Unit

    override fun captureMessage(message: String) = Unit

    override fun setScopeTag(tag: String, value: String) = Unit

    override fun removeScopeTag(tag: String) = Unit
}
