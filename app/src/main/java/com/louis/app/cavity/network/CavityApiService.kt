package com.louis.app.cavity.network

import com.louis.app.cavity.network.response.LoginResponse
import retrofit2.http.POST

interface CavityApiService {
    @POST("account")
    suspend fun register(email: String, password: String)

    @POST("account/confirm")
    suspend fun confirmAccount(email: String, registrationCode: String)

    @POST("auth/login")
    suspend fun login(email: String, password: String): LoginResponse
}
