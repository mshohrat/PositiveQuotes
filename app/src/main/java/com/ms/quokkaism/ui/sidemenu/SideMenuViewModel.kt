package com.ms.quokkaism.ui.sidemenu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ms.quokkaism.model.Profile
import com.orhanobut.hawk.Hawk

class SideMenuViewModel : ViewModel() {

    private val _profile = MutableLiveData(Hawk.get(Profile.PROFILE_KEY) as? Profile)

    val profile: LiveData<Profile?> = _profile
}