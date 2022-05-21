package com.louis.app.cavity.network.response

data class LoginResponse(val email: String, val token: String)

data class UserResponse(val email: String)

data class ConfirmResponse(val token: String)
