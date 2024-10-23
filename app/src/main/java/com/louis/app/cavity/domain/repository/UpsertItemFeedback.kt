package com.louis.app.cavity.domain.repository

interface UpsertItemFeedback {
    fun onSuccess()
    fun onItemAlreadyExists()
    fun onItemInvalidName()
    fun onError()
}
