package com.ms.quokkaism.model

import com.google.gson.annotations.SerializedName

data class SyncLikeAction(
    @SerializedName("id")
    val id: Long,
    @SerializedName("liked")
    val liked: Boolean
)