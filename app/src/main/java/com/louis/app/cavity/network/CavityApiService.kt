package com.louis.app.cavity.network

import com.louis.app.cavity.model.*
import com.louis.app.cavity.network.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface CavityApiService {
    @POST("account")
    suspend fun register(@Body parameters: Map<String, String>)

    @POST("account/confirm")
    suspend fun confirmAccount(@Body parameters: Map<String, String>): LoginResponse

    @POST("auth/login")
    suspend fun login(@Body parameters: Map<String, String>): LoginResponse

    @POST("account/recover")
    suspend fun recoverAccount(@Body parameters: Map<String, String>)

    @POST("account/delete")
    suspend fun deleteAccount(@Body parameters: Map<String, String>)

    @POST("account/lastuser")
    suspend fun postAccountLastUser(@Body parameters: Map<String, String>)

    @POST("county")
    suspend fun postCounties(@Body counties: List<County>)

    @POST("wine")
    suspend fun postWines(@Body wines: List<Wine>)

    @POST("bottle")
    suspend fun postBottles(@Body bottles: List<Bottle>)

    @POST("friend")
    suspend fun postFriends(@Body friends: List<Friend>)

    @POST("grape")
    suspend fun postGrapes(@Body grapes: List<Grape>)

    @POST("review")
    suspend fun postReviews(@Body reviews: List<Review>)

    @POST("history")
    suspend fun postHistoryEntries(@Body entries: List<HistoryEntry>)

    @POST("tasting")
    suspend fun postTastings(@Body tastings: List<Tasting>)

    @POST("tasting-action")
    suspend fun postTastingActions(@Body tastings: List<TastingAction>)

    @POST("freview")
    suspend fun postFReview(@Body fReviews: List<FReview>)

    @POST("qgrape")
    suspend fun postQGrapes(@Body qGrapes: List<QGrape>)

    @POST("tasting-x-friend")
    suspend fun postTastingFriendsXRef(@Body tastingXFriends: List<TastingXFriend>)

    @POST("history-x-friend")
    suspend fun postHistoryFriendsXRef(@Body historyXFriends: List<HistoryXFriend>)

    @GET("account")
    suspend fun getAccount(): LoginResponse

    @GET("county")
    suspend fun getCounties(): List<County>

    @GET("wine")
    suspend fun getWines(): List<Wine>

    @GET("bottle")
    suspend fun getBottles(): List<Bottle>

    @GET("friend")
    suspend fun getFriends(): List<Friend>

    @GET("grape")
    suspend fun getGrapes(): List<Grape>

    @GET("review")
    suspend fun getReviews(): List<Review>

    @GET("history")
    suspend fun getHistoryEntries(): List<HistoryEntry>

    @GET("tasting")
    suspend fun getTastings(): List<Tasting>

    @GET("tasting-action")
    suspend fun getTastingActions(): List<TastingAction>

    @GET("freview")
    suspend fun getFReviews(): List<FReview>

    @GET("qgrape")
    suspend fun getQGrapes(): List<QGrape>

    @GET("tasting-x-friend")
    suspend fun getTastingFriendsXRef(): List<TastingXFriend>

    @GET("history-x-friend")
    suspend fun getHistoryFriendsXRef(): List<HistoryXFriend>

    @DELETE("county")
    suspend fun deleteCounties()

    @DELETE("wine")
    suspend fun deleteWines()

    @DELETE("bottle")
    suspend fun deleteBottles()

    @DELETE("friend")
    suspend fun deleteFriends()

    @DELETE("grape")
    suspend fun deleteGrapes()

    @DELETE("review")
    suspend fun deleteReviews()

    @DELETE("history")
    suspend fun deleteHistoryEntries()

    @DELETE("tasting")
    suspend fun deleteTastings()

    @DELETE("tasting-action")
    suspend fun deleteTastingActions()

    @DELETE("freview")
    suspend fun deleteFReviews()

    @DELETE("qgrape")
    suspend fun deleteQGrapes()

    @DELETE("tasting-x-friend")
    suspend fun deleteTastingFriendsXRef()

    @DELETE("history-x-friend")
    suspend fun deleteHistoryFriendsXRef()
}
