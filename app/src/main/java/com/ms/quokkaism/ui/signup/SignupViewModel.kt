package com.ms.quokkaism.ui.signup

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ms.quokkaism.R
import com.ms.quokkaism.model.Profile
import com.ms.quokkaism.network.base.ApiServiceGenerator
import com.ms.quokkaism.network.model.GeneralResponse
import com.ms.quokkaism.network.model.SignupRequest
import com.orhanobut.hawk.Hawk
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SignupViewModel : ViewModel() {

    private val _signup = MutableLiveData<GeneralResponse?>()
    val signup : LiveData<GeneralResponse?> = _signup

    private val _signup_error = MutableLiveData<GeneralResponse?>()
    val signup_error : LiveData<GeneralResponse?> = _signup_error

    @SuppressLint("CheckResult")
    fun signup(name: String?, email: String?, password: String?, repeatPassword: String?)
    {
        when {
            name.isNullOrEmpty() -> { _signup_error.value = GeneralResponse(messageResId = R.string.name_is_required) }
            email.isNullOrEmpty() -> { _signup_error.value = GeneralResponse(messageResId = R.string.email_is_required) }
            password.isNullOrEmpty() -> { _signup_error.value = GeneralResponse(messageResId = R.string.password_is_required) }
            repeatPassword.isNullOrEmpty() -> { _signup_error.value = GeneralResponse(messageResId = R.string.repeat_password_is_required) }
            (password == repeatPassword).not() -> { _signup_error.value = GeneralResponse(messageResId = R.string.password_and_repeat_are_not_the_same)}
            else -> {
                ApiServiceGenerator.getApiService.signup(SignupRequest(name,email,password))
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe({
                        updateProfile(name,email)
                        _signup.value = GeneralResponse(messageResId = R.string.registered_successfully)
                    },{
                        _signup_error.value = GeneralResponse(it.message)
                    })
            }
        }
    }

    private fun updateProfile(name: String?, email: String?) {
        if (Hawk.contains(Profile.PROFILE_KEY) && Hawk.get<Profile?>(Profile.PROFILE_KEY) is Profile) {
            val profile = Hawk.get<Profile?>(Profile.PROFILE_KEY)
            profile?.name = name
            profile?.email = email
            profile?.isGuest = false
            Hawk.put(Profile.PROFILE_KEY,profile)
        }
    }

    private fun getUUID() : String {
        if (Hawk.contains(Profile.PROFILE_KEY) && Hawk.get<Profile?>(Profile.PROFILE_KEY) is Profile) {
            val profile = Hawk.get<Profile?>(Profile.PROFILE_KEY)
            return profile?.uuid?.let { it } ?: ""
        }
        return ""
    }
}
