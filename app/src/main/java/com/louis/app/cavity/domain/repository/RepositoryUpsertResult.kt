package com.louis.app.cavity.domain.repository

import android.database.sqlite.SQLiteConstraintException
import com.louis.app.cavity.domain.error.ErrorReporter

sealed class RepositoryUpsertResult<out T> {
    data class Success<out T>(val value: T) : RepositoryUpsertResult<T>()
    data object AlreadyExists : RepositoryUpsertResult<Nothing>()
    data object InvalidName : RepositoryUpsertResult<Nothing>()
    data object Failure : RepositoryUpsertResult<Nothing>()

    companion object {
        fun handleDatabaseError(
            t: Throwable,
            errorReporter: ErrorReporter
        ): RepositoryUpsertResult<Nothing> {
            return when (t) {
                is SQLiteConstraintException -> AlreadyExists
                else -> {
                    errorReporter.captureException(t)
                    Failure
                }
            }
        }
    }
}

