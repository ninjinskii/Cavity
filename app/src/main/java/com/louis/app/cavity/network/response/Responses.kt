package com.louis.app.cavity.network.response

data class LoginResponse(val email: String, val token: String)

data class FileTransfer(val extension: String, val content: String)
