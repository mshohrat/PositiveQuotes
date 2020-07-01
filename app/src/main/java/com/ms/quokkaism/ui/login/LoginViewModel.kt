package com.ms.quokkaism.ui.login

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ms.quokkaism.network.base.ApiServiceGenerator
import com.ms.quokkaism.network.model.GeneralResponse
import com.ms.quokkaism.network.model.LoginRequest
import com.ms.quokkaism.network.model.LoginResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class LoginViewModel : ViewModel() {

    private val _login = MutableLiveData<LoginResponse?>()
    val login : LiveData<LoginResponse?> = _login

    private val _login_error = MutableLiveData<GeneralResponse?>()
    val login_error : LiveData<GeneralResponse?> = _login_error

    @SuppressLint("CheckResult")
    fun login(email : String,password: String)
    {
        ApiServiceGenerator.getApiService.login(LoginRequest(email,password))
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                _login.value = it
            },{
                _login_error.value = GeneralResponse(it.message)
            })

    }
}
