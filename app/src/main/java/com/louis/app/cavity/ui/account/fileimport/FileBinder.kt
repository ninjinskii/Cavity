package com.louis.app.cavity.ui.account.fileimport

import android.net.Uri
import com.louis.app.cavity.db.WineRepository

interface FileBinder {
    suspend fun bind(repository: WineRepository, uri: Uri)

    fun getBindedObjectId(uri: Uri): Long {
        val str = uri.toString()
        val end = str.split("-").last().split(".").first()

        return if (end.startsWith("f")) {
            end.substring(1).toLong()
        } else {
            end.toLong()
        }
    }
}
