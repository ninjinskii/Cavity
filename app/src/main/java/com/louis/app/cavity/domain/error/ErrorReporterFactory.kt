package com.louis.app.cavity.domain.error

import android.app.Application
import android.content.Context
import com.louis.app.cavity.domain.repository.PrefsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object ErrorReporterFactory {
    fun create(context: Context): ErrorReporter {
        val prefsRepository =
            PrefsRepository.getInstance(context.applicationContext as Application)

        val consentToErrorReports =
            runBlocking { prefsRepository.errorReportingConsent.first() }

        return if (consentToErrorReports) {
//            ErrorReporterFactory.create(context)
            FakeErrorReporter()
        } else {
            LoggerErrorReporter()
        }
    }
}
