package com.louis.app.cavity.domain.error

import android.content.Context
import com.louis.app.cavity.BuildConfig
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid

class SentryErrorReporter private constructor(context: Context) : ErrorReporter {

    companion object {
        @Volatile
        private var instance: SentryErrorReporter? = null

        fun getInstance(context: Context): SentryErrorReporter {
            return instance ?: synchronized(this) {
                instance ?: SentryErrorReporter(context).also { instance = it }
            }
        }
    }

    init {
        SentryAndroid.init(context) { options ->
            options.beforeSend = SentryOptions.BeforeSendCallback { event, _ ->
                if (stopEvents()) null else event
            }
        }
    }

    override fun stopEvents() = BuildConfig.DEBUG

    override fun captureException(throwable: Throwable) {
        Sentry.captureException(throwable)
    }

    override fun captureMessage(message: String) {
        Sentry.captureMessage(message, SentryLevel.INFO)
    }

    override fun setScopeTag(tag: String, value: String) {
        Sentry.configureScope { scope -> scope.setTag(tag, value) }
    }

    override fun removeScopeTag(tag: String) {
        Sentry.configureScope { scope -> scope.removeTag(tag) }
    }
}
