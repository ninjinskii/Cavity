package com.louis.app.cavity.db

import android.app.Application
import com.louis.app.cavity.R
import com.louis.app.cavity.model.*
import com.louis.app.cavity.network.CavityApiClient
import com.louis.app.cavity.network.CavityApiService
import com.louis.app.cavity.network.response.ApiResponse
import com.louis.app.cavity.network.response.LoginResponse
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

    suspend fun postCounties(counties: List<County>): ApiResponse<Unit> {
        return doApiCall { cavityApi.postCounties(counties) }
    }

    suspend fun postWines(wines: List<Wine>): ApiResponse<Unit> {
        return doApiCall { cavityApi.postWines(wines) }
    }

    suspend fun postBottles(bottles: List<Bottle>): ApiResponse<Unit> {
        return doApiCall { cavityApi.postBottles(bottles) }
    }

    suspend fun postFriends(friends: List<Friend>): ApiResponse<Unit> {
        return doApiCall { cavityApi.postFriends(friends) }
    }

    suspend fun postGrapes(grapes: List<Grape>): ApiResponse<Unit> {
        return doApiCall { cavityApi.postGrapes(grapes) }
    }

    suspend fun postReviews(reviews: List<Review>): ApiResponse<Unit> {
        return doApiCall { cavityApi.postReviews(reviews) }
    }

    suspend fun postHistoryEntries(entries: List<HistoryEntry>): ApiResponse<Unit> {
        return doApiCall { cavityApi.postHistoryEntries(entries) }
    }

    suspend fun postTastings(tastings: List<Tasting>): ApiResponse<Unit> {
        return doApiCall { cavityApi.postTastings(tastings) }
    }

    suspend fun postTastingActions(actions: List<TastingAction>): ApiResponse<Unit> {
        return doApiCall { cavityApi.postTastingActions(actions) }
    }

    suspend fun postFReviews(fReviews: List<FReview>): ApiResponse<Unit> {
        return doApiCall { cavityApi.postFReview(fReviews) }
    }

    suspend fun postQGrapes(qGrapes: List<QGrape>): ApiResponse<Unit> {
        return doApiCall { cavityApi.postQGrapes(qGrapes) }
    }

    suspend fun postTastingFriendsXRefs(tastingFriendXRefs: List<TastingXFriend>):
        ApiResponse<Unit> {
        return doApiCall { cavityApi.postTastingFriendsXRef(tastingFriendXRefs) }
    }

    suspend fun postHistoryFriendsXRefs(historyFriendXRefs: List<HistoryXFriend>):
        ApiResponse<Unit> {
        return doApiCall { cavityApi.postHistoryFriendsXRef(historyFriendXRefs) }
    }

    private suspend fun <T> doApiCall(apiCall: suspend () -> T): ApiResponse<T> {
        return try {
            ApiResponse.Success(apiCall.invoke())
        } catch (t: Throwable) {
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
