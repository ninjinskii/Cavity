package com.louis.app.cavity.db

import android.app.Application
import com.louis.app.cavity.R
import com.louis.app.cavity.network.CavityApiClient
import com.louis.app.cavity.network.CavityApiService
import com.louis.app.cavity.network.response.ApiResponse
import com.louis.app.cavity.network.response.LoginResponse
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create

class AccountRepository private constructor(private val app: Application) {
    companion object {
        @Volatile
        var instance: AccountRepository? = null

        fun getInstance(app: Application) =
            instance ?: synchronized(this) {
                instance ?: AccountRepository(app).also { instance = it }
            }
    }

    private lateinit var retrofit: Retrofit
    private lateinit var cavityApi: CavityApiService

    fun submitIpAndRetrieveToken(ip: String, token: String) {
        retrofit = CavityApiClient.buildRetrofitInstance(ip, token)
        cavityApi = retrofit.create()
    }

    suspend fun login(email: String, password: String): ApiResponse<LoginResponse> {
        return doApiCall { cavityApi.login(email, password) }
    }

    suspend fun register(email: String, password: String): ApiResponse<Unit> {
        return doApiCall { cavityApi.register(email, password) }
    }

    suspend fun confirmAccount(email: String, registrationCode: String): ApiResponse<Unit> {
        return doApiCall { cavityApi.confirmAccount(email, registrationCode) }
    }

    private suspend fun <T> doApiCall(apiCall: suspend () -> T): ApiResponse<T> {
        return try {
            ApiResponse.Success(apiCall.invoke())
        } catch (t: Throwable) {
            when (t) {
                is HttpException -> parseError(t.response())
                else -> ApiResponse.UnknownError
//                is IOException -> ApiResponse.UnknownError
//                else -> throw t
            }
        }
    }

    private fun parseError(response: Response<*>?): ApiResponse.Failure {
        return try {
            val converter = retrofit.responseBodyConverter<ApiResponse.Failure>(
                ApiResponse.Failure::class.java,
                emptyArray()
            )

            val message = response?.errorBody()?.let {
                converter.convert(it)?.message
            } ?: app.getString(R.string.base_error)

            ApiResponse.Failure(message)

        } catch (e: IllegalArgumentException) {
            ApiResponse.Failure(app.getString(R.string.base_error))
        }
    }
}
