package com.ms.quokkaism.network.model

import com.ms.quokkaism.BuildConfig
import com.ms.quokkaism.model.SyncLikeAction
import com.google.gson.annotations.SerializedName as SN

data class SignupRequest(
    @SN("name")
    val name: String? = null,
    @SN("email")
    val email: String? = null,
    @SN("password")
    val password: String? = null
//    @SN("uuid")
//    val uuid: String? = null
)

data class LoginRequest(
    @SN("email")
    val email: String? = null,
    @SN("password")
    val password: String? = null,
    @SN("grant_type")
    val grantType : String = "password",
    @SN("client_id")
    val clientId: String = BuildConfig.CLIENT_ID,
    @SN("client_secret")
    val clientSecret: String = BuildConfig.CLIENT_SECRET
)

data class RefreshTokenRequest(
    @SN("refresh_token")
    val refreshToken: String? = null,
    @SN("grant_type")
    val grantType: String = "refresh_token",
    @SN("client_id")
    val clientId: String = BuildConfig.CLIENT_ID,
    @SN("client_secret")
    val clientSecret: String = BuildConfig.CLIENT_SECRET
)

data class LoginAsGuestRequest(
    @SN("uuid")
    val uuid: String? = null,
    @SN("client_id")
    val clientId: String = BuildConfig.CLIENT_ID,
    @SN("client_secret")
    val clientSecret: String = BuildConfig.CLIENT_SECRET
)

data class EditProfileRequest(
    @SN("name")
    val name: String? = null,
    @SN("gender")
    val gender: Int
)

data class RegisterFbTokenRequest(
    @SN("token")
    val token: String
)

data class ChangeSettingRequest(
    @SN("interval")
    val interval: Int
)

data class SyncLikeActionsRequest(
    @SN("actions")
    val actions: List<SyncLikeAction>
)