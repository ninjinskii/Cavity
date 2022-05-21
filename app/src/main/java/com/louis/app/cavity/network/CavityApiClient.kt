package com.louis.app.cavity.network

import com.louis.app.cavity.db.PrefsRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object CavityApiClient {
    private const val API_URL = "https://cavity.fr"

    private val moshiConverter: MoshiConverterFactory by lazy {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        MoshiConverterFactory.create(moshi)
    }

    fun buildRetrofitInstance(locale: String, prefsRepository: PrefsRepository): Retrofit {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor {
                val token = prefsRepository.getApiToken()
                val request = it.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Accept-Language", locale)
                    .build()

                it.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(API_URL)
            .client(httpClient)
            .addConverterFactory(moshiConverter)
            .build()
    }
}
