package com.louis.app.cavity.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object CavityApiClient {
    private val moshiConverter: MoshiConverterFactory by lazy {
        MoshiConverterFactory.create()
    }

    fun buildRetrofitInstance(ip: String, token: String): Retrofit {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor {
                val request = it.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                it.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(ip)
            .client(httpClient)
            .addConverterFactory(moshiConverter)
            .build()
            .also { it.create(CavityApiService::class.java) }
    }
}
