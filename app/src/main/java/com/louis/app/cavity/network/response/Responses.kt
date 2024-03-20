package com.louis.app.cavity.network.response

data class LoginResponse(
    val email: String,
    val token: String,
    val lastUser: String?,
    val lastUpdateTime: Long?
)

//data class UserResponse(val email: String)

//data class AccountResponse(val email: String, val lastUser: String?, val lastUpdateTime: Long?)

//data class ConfirmResponse(val token: String)
