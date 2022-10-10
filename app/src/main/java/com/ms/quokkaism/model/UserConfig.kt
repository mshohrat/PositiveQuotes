package com.ms.quokkaism.model

data class UserConfig(
    val isUserActive : Boolean
) {
    companion object {
        const val USER_CONFIG_KEY = "User Config Key"
    }
}