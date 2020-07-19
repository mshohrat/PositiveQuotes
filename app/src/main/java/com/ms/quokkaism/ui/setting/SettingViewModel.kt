package com.ms.quokkaism.ui.setting

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ms.quokkaism.R
import com.ms.quokkaism.extension.isDeviceOnline
import com.ms.quokkaism.model.ProfileSetting
import com.ms.quokkaism.network.base.ApiServiceGenerator
import com.ms.quokkaism.network.model.ChangeSettingRequest
import com.ms.quokkaism.network.model.GeneralResponse
import com.orhanobut.hawk.Hawk
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SettingViewModel : ViewModel() {

    var isSettingChanged = MutableLiveData<Boolean>(false)

    var settingInterval: Int = 0
    set(value) {
        isSettingChanged.value = value != _setting.value?.interval
        field = value
    }

    private val _setting = MutableLiveData<ProfileSetting?>()
    val setting : LiveData<ProfileSetting?> = _setting

    private val _settingError = MutableLiveData<GeneralResponse?>()
    val settingError : LiveData<GeneralResponse?> = _settingError

    init {
        if (Hawk.contains(ProfileSetting.PROFILE_SETTING_KEY) && Hawk.get<ProfileSetting?>(ProfileSetting.PROFILE_SETTING_KEY) is ProfileSetting) {
            val setting = Hawk.get<ProfileSetting?>(ProfileSetting.PROFILE_SETTING_KEY)
            _setting.value = setting
        }
    }

    @SuppressLint("CheckResult")
    fun changeSetting()
    {
        if(isSettingChanged.value == true)
        {
            if(isDeviceOnline()) {
                ApiServiceGenerator.getApiService.changeSetting(ChangeSettingRequest(settingInterval))
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe({
                        it?.setting?.interval?.let {
                            val s = ProfileSetting(it)
                            Hawk.put(ProfileSetting.PROFILE_SETTING_KEY, s)
                            _setting.value = s
                            isSettingChanged.value = false
                        } ?: kotlin.run {
                            _settingError.value =
                                GeneralResponse(messageResId = R.string.error_on_connecting_to_server)
                        }
                    }, {
                        _settingError.value = GeneralResponse(it.message)
                    })
            }
            else {
                val s = ProfileSetting(settingInterval)
                Hawk.put(ProfileSetting.PROFILE_SETTING_KEY, s)
                _setting.value = s
                isSettingChanged.value = false
            }
        }
        else
        {
            _settingError.value = GeneralResponse(messageResId = R.string.you_should_change_setting_from_previous_version)
        }
    }

}
