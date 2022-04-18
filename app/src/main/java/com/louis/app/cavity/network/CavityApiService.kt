package com.louis.app.cavity.network

import com.louis.app.cavity.network.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface CavityApiService {
    @POST("account")
    suspend fun register(@Body parameters: Map<String, String>)

    @POST("account/confirm")
    suspend fun confirmAccount(@Body parameters: Map<String, String>)

    @POST("auth/login")
    suspend fun login(@Body parameters: Map<String, String>): LoginResponse
}
