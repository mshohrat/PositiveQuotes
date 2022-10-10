package com.ms.quokkaism.model

data class ProfileSetting(
    var interval : Int = INTERVAL_12_HOURS
) {
    companion object {
        const val PROFILE_SETTING_KEY = "Profile Setting Key"
        const val NOTIFICATIONS_ARE_SET_KEY = "Notifications Are Set Key"
        const val INTERVAL_DAILY = 24
        const val INTERVAL_12_HOURS = 12
        const val INTERVAL_8_HOURS = 8
        const val INTERVAL_6_HOURS = 6
        const val INTERVAL_4_HOURS = 4
        const val INTERVAL_3_HOURS = 3
    }
}