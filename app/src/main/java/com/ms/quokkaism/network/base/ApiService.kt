package com.ms.quokkaism.network.base

import com.ms.quokkaism.BuildConfig
import com.ms.quokkaism.network.model.*
import io.reactivex.Single
import retrofit2.http.*

interface ApiService {

    @POST("auth/login-as-guest")
    fun loginAsGuest(@Body request: LoginAsGuestRequest): Single<LoginResponse?>?

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Single<LoginResponse?>?

    @POST("oauth/token")

    fun refreshToken(@Body request: RefreshTokenRequest,@Url url: String = BuildConfig.SERVER_ADDRESS): Single<RefreshTokenResponse?>?

    @POST("signup-from-guest")
    fun signup(@Body request: SignupRequest): Single<GeneralResponse?>?

    @GET("profile")
    fun getProfile(): Single<ProfileResponse?>?

    @GET("likes/{page}")
    fun getLikedQuotes(@Path("page") page: Int): Single<LikedQuotesResponse?>?

    @PUT("profile")
    fun editProfile(@Body request: EditProfileRequest): Single<ProfileResponse?>?

    @GET("config")
    fun getConfig() : Single<ConfigResponse?>?

    @POST("fb-token")
    fun registerFbToken(@Body request: RegisterFbTokenRequest) : Single<GeneralResponse?>?

    @PATCH("edit-setting")
    fun changeSetting(@Body request: ChangeSettingRequest): Single<EditSettingResponse?>?

    @GET("quotes")
    fun getQuotes() : Single<QuoteListResponse?>?

    @PATCH("like/{id}")
    fun like(@Path("id")quoteId: Long) : Single<GeneralResponse?>?

    @PATCH("dislike/{id}")
    fun dislike(@Path("id") quoteId: Long) : Single<GeneralResponse?>?

    @POST("likes/sync")
    fun syncLikeActions(@Body request: SyncLikeActionsRequest) : Single<SyncLikeActionsResponse?>?
}