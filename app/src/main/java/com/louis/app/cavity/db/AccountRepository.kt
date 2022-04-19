package com.louis.app.cavity.db

import android.app.Application
import com.louis.app.cavity.R
import com.louis.app.cavity.network.CavityApiClient
import com.louis.app.cavity.network.CavityApiService
import com.louis.app.cavity.network.response.ApiResponse
import com.louis.app.cavity.network.response.LoginResponse
import com.louis.app.cavity.util.L
import okhttp3.ResponseBody
import retrofit2.*

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
        val locale = app.getString(R.string.locale)
        retrofit = CavityApiClient.buildRetrofitInstance(ip, token, locale)
        cavityApi = retrofit.create()
    }

    suspend fun login(email: String, password: String): ApiResponse<LoginResponse> {
        val parameters = mapOf("email" to email, "password" to password)
        return doApiCall { cavityApi.login(parameters) }
    }

    suspend fun register(email: String, password: String): ApiResponse<Unit> {
        val parameters = mapOf("email" to email, "password" to password)
        return doApiCall { cavityApi.register(parameters) }
    }

    suspend fun confirmAccount(email: String, registrationCode: String): ApiResponse<Unit> {
        val parameters = mapOf("email" to email, "registrationCode" to registrationCode)
        return doApiCall { cavityApi.confirmAccount(parameters) }
    }

    private suspend fun <T> doApiCall(apiCall: suspend () -> T): ApiResponse<T> {
        return try {
            ApiResponse.Success(apiCall.invoke())
        } catch (t: Throwable) {
            L.e(t)
            when (t) {
                is HttpException -> when (t.code()) {
                    412 -> ApiResponse.UnregisteredError
                    else -> parseError(t.response())
                }
                else -> ApiResponse.UnknownError
//                is IOException -> ApiResponse.UnknownError
//                else -> throw t
            }
        }
    }

    private fun parseError(response: Response<*>?): ApiResponse.Failure {
        return try {
            val converter: Converter<ResponseBody, ApiResponse.Failure> = retrofit
                .responseBodyConverter(
                    ApiResponse.Failure::class.java,
                    arrayOfNulls<Annotation>(0)
                )

            val message = response?.errorBody()?.let {
                converter.convert(it)!!.message
            } ?: app.getString(R.string.base_error)

            ApiResponse.Failure(message)

        } catch (e: IllegalArgumentException) {
            ApiResponse.Failure(app.getString(R.string.base_error))
        }
    }
}
