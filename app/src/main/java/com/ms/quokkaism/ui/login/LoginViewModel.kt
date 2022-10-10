package com.ms.quokkaism.ui.login

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ms.quokkaism.R
import com.ms.quokkaism.extension.getErrorHttpModel
import com.ms.quokkaism.extension.getFirstMessage
import com.ms.quokkaism.extension.isUnauthorizedError
import com.ms.quokkaism.network.base.ApiServiceGenerator
import com.ms.quokkaism.network.model.GeneralResponse
import com.ms.quokkaism.network.model.InvalidRequestResponse
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
                it.getErrorHttpModel(InvalidRequestResponse::class.java)?.let {
                    it.getFirstMessage()?.let { message ->
                        _login_error.value = GeneralResponse(message)
                    }
                } ?: kotlin.run {
                    if(it.isUnauthorizedError()) {
                        _login_error.value = GeneralResponse(messageResId = R.string.username_or_password_is_incorrect)
                    } else {
                        _login_error.value = GeneralResponse(messageResId = R.string.error_on_connecting_to_server)
                    }
                }
            })

    }
}
