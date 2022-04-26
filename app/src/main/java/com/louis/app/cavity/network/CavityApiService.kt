package com.louis.app.cavity.network

import com.louis.app.cavity.model.*
import com.louis.app.cavity.network.response.FileTransfer
import com.louis.app.cavity.network.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface CavityApiService {
    @POST("account")
    suspend fun register(@Body parameters: Map<String, String>)

    @POST("account/confirm")
    suspend fun confirmAccount(@Body parameters: Map<String, String>)

    @POST("auth/login")
    suspend fun login(@Body parameters: Map<String, String>): LoginResponse

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

    @POST("wine/{wineId}/image")
    suspend fun postWineImage(@Path("wineId") wineId: String, @Body file: FileTransfer)
}
