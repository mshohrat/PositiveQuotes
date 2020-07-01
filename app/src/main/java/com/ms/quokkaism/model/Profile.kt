package com.ms.quokkaism.model

import com.orhanobut.hawk.Hawk

data class Profile(
    var name : String? = null,
    var email : String? = null,
    var gender : Int = GENDER_UNKNOWN,
    var token : String? = null,
    var refreshToken : String? = null,
    var expiresIn : Long = 300000,
    var isGuest : Boolean = true,
    var uuid : String? = null) {

    companion object {
        val PROFILE_KEY = "Profile Key"
        fun isUserGuest() : Boolean {
            if(Hawk.contains(PROFILE_KEY) && Hawk.get<Profile?>(PROFILE_KEY) is Profile &&
                (Hawk.get(PROFILE_KEY) as Profile).isGuest.not()) {
                return false
            }
            return true
        }
        const val GENDER_MAIL = 1
        const val GENDER_FEMALE = 2
        const val GENDER_UNKNOWN = 0
    }
}