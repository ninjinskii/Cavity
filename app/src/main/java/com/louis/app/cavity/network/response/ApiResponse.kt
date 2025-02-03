package com.louis.app.cavity.network.response

sealed class ApiResponse<out T> {
    data class Success<out T>(val value: T) : ApiResponse<T>()
    data class Failure(val message: String) : ApiResponse<Nothing>()
    data object UnknownError : ApiResponse<Nothing>()
    data object UnregisteredError : ApiResponse<Nothing>()
    data object UnauthorizedError : ApiResponse<Nothing>()
}
