package com.louis.app.cavity.network.response

data class LoginResponse(
    val email: String,
    val token: String,
    val lastUser: String?,
    val lastUpdateTime: Long?
)
