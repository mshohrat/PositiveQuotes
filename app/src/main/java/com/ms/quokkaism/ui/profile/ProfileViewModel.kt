package com.ms.quokkaism.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ms.quokkaism.model.Profile
import com.orhanobut.hawk.Hawk

class ProfileViewModel : ViewModel() {

    private val _profile = MutableLiveData<Profile?>()

    val profile : LiveData<Profile?> = _profile

    init {
        if(Hawk.contains(Profile.PROFILE_KEY) && Hawk.get<Profile?>(Profile.PROFILE_KEY) is Profile)
        {
            _profile.value = Hawk.get(Profile.PROFILE_KEY) as Profile
        }
    }
}
