package com.ms.quokkaism.ui.splash

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ms.quokkaism.extension.isDeviceOnline
import com.ms.quokkaism.network.base.ApiServiceGenerator
import com.ms.quokkaism.network.model.ConfigResponse
import com.ms.quokkaism.network.model.GeneralResponse
import com.ms.quokkaism.network.model.LoginAsGuestRequest
import com.ms.quokkaism.network.model.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class SplashViewModel : ViewModel() {
    private val _config = MutableLiveData<ConfigResponse?>()
    val config : LiveData<ConfigResponse?> = _config

    private val _config_error = MutableLiveData<GeneralResponse?>()
    val config_error : LiveData<GeneralResponse?> = _config_error

    @SuppressLint("CheckResult")
    fun refreshConfig() {
        if(isDeviceOnline()) {
            ApiServiceGenerator.getApiService.getConfig()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({
                    _config.value = it
                }, {
                    _config_error.value = GeneralResponse(it.message)
                })
        }
        else
        {
            _config.value = null
        }
    }

    private val _login_as_guest = MutableLiveData<LoginResponse?>()
    val login_as_guest : LiveData<LoginResponse?> = _login_as_guest

    private val _login_as_guest_error = MutableLiveData<GeneralResponse?>()
    val login_as_guest_error : LiveData<GeneralResponse?> = _login_as_guest_error

    @SuppressLint("CheckResult")
    fun loginAsGuest() {
        ApiServiceGenerator.getApiService.loginAsGuest(
            LoginAsGuestRequest(UUID.randomUUID().toString())
        )
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                _login_as_guest.value = it
            },{
                _login_as_guest_error.value = GeneralResponse(it.message)
            })
    }

}
