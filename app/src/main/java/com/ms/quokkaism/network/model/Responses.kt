package com.ms.quokkaism.network.model

import com.ms.quokkaism.model.Profile
import com.ms.quokkaism.model.ProfileSetting
import com.ms.quokkaism.model.SyncLikeAction
import com.google.gson.annotations.SerializedName as SN

data class LoginResponse(
    @SN("name")
    val name: String? = null,
    @SN("email")
    val email: String? = null,
    @SN("is_guest")
    val isGuest: Boolean = true,
    @SN("access_token")
    val token: String? = null,
    @SN("refresh_token")
    val refreshToken: String? = null,
    @SN("expires_in")
    val expiresIn: Long = 3000000,
    @SN("uuid")
    val uuid: String? = null
)

data class RefreshTokenResponse(
    @SN("access_token")
    val token: String? = null,
    @SN("refresh_token")
    val refreshToken: String? = null,
    @SN("expires_in")
    val expiresIn: Long = 3000000
)

data class ProfileResponse(
    @SN("name")
    val name: String? = null,
    @SN("email")
    val email: String? = null,
    @SN("gender")
    val gender: Int = Profile.GENDER_UNKNOWN
)

data class QuoteResponse(
    @SN("id")
    val id: Long? = null,
    @SN("text")
    val text: String? = null,
    @SN("author")
    val author: String? = null
)

data class QuoteListResponse(
    @SN("quotes")
    val quoteList: List<QuoteResponse>? = null
)

data class LikedQuotesResponse(
    @SN("current_page")
    val currentPage: Int = 1,
    @SN("total_pages")
    val totalPages: Int = 1,
    @SN("data")
    val likedQuotes: List<QuoteResponse?>?
)

data class SettingResponse(
    @SN("interval")
    val interval: Int? = ProfileSetting.INTERVAL_12_HOURS
)

data class GeneralResponse(
    @SN("message")
    val message: String? = null,
    val messageResId: Int? = null
)

data class ConfigResponse(
    @SN("is_user_active")
    val isUserActive : Boolean,
    @SN("requires_token")
    val requiresFbToken : Boolean = false,
    @SN("setting")
    val setting: SettingResponse? = null
)

data class SyncLikeActionsResponse(
    @SN("actions_synced")
    val actions: List<SyncLikeAction>
)

data class ReceivedNotificationData(
    @SN("quotes")
    val quotes: List<ReceivedQuote?>?
)

data class ReceivedQuote(
    @SN("id")
    val id: Long? = null,
    @SN("text")
    val text: String? = null,
    @SN("author")
    val author: String? = null
)