package com.louis.app.cavity.domain.repository

import android.app.Application
import androidx.room.withTransaction
import com.louis.app.cavity.db.CavityDatabase
import com.louis.app.cavity.domain.error.ErrorReporter
import com.louis.app.cavity.domain.error.SentryErrorReporter

open class Repository(app: Application) {
    protected val database = CavityDatabase.getInstance(app)
    protected val errorReporter: ErrorReporter = SentryErrorReporter.getInstance(app)

    suspend fun <T> transaction(databaseQueries: suspend () -> T) = database.withTransaction {
        databaseQueries()
    }

    protected suspend fun <T> assertTransaction(block: suspend () -> T): T {
        if (!database.inTransaction()) {
            throw IllegalStateException("This method should be called inside transaction")
        }

        return block()
    }
}
