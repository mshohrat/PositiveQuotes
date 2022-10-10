package com.ms.quokkaism.extension

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.gson.Gson
import com.ms.quokkaism.App
import com.ms.quokkaism.MainActivity
import com.ms.quokkaism.model.Profile
import com.ms.quokkaism.network.model.GeneralResponse
import com.ms.quokkaism.network.model.InvalidRequestResponse
import com.ms.quokkaism.util.LoadingDialog
import com.orhanobut.hawk.Hawk
import retrofit2.HttpException
import retrofit2.Response


fun convertPixelToDp(context: Context?, px: Float): Float {
    if (context?.resources == null) {
        return px
    }
    val resources: Resources = context.resources
    val metrics: DisplayMetrics = resources.displayMetrics
    return px / (metrics.densityDpi / 160f)
}

fun convertDpToPixel(context: Context?, dp: Float): Float {
    if (context?.resources == null) {
        return dp
    }
    val resources: Resources = context.resources
    val metrics: DisplayMetrics = resources.displayMetrics
    return dp * (metrics.densityDpi / 160f)
}

fun Fragment.navigate(destinationId: Int){
    val navController = findNavController()
    activity?.takeIf { it.isFinishing.not() }?.let {
        navController.navigate(destinationId)
    }
}

fun Fragment.navigateUp(){
    val navController = findNavController()
    activity?.takeIf { it.isFinishing.not() }?.let {
        navController.navigateUp()
    }
}

fun Fragment.navigate(destinationId: Int,arguments : Bundle?){
    val navController = findNavController()
    activity?.takeIf { it.isFinishing.not() }?.let {
        navController.navigate(destinationId,arguments)
    }
}

fun Fragment.reloadSideMenu() {
    activity?.takeIf { it.isFinishing.not() && it is MainActivity }?.let {
        (it as MainActivity).reloadSideMenu()
    }
}

fun Fragment.showLoadingDialog(@StringRes messageResId : Int? = null) : LoadingDialog? {
    activity?.takeIf { it.isFinishing.not() }?.let {
        val dialog = LoadingDialog(it,messageResId)
        dialog.show()
        return dialog
    } ?: kotlin.run {
        return null
    }
}

fun Fragment.dismissDialog(dialog: Dialog?) {
    dialog?.takeIf { it.isShowing.not() }?.dismiss()
    dialog?.cancel()
}

fun isUserLoggedIn() : Boolean {
    if(Hawk.contains(Profile.PROFILE_KEY) && Hawk.get<Profile?>(Profile.PROFILE_KEY) is Profile &&
        (Hawk.get(Profile.PROFILE_KEY) as Profile).token.isNullOrEmpty().not()) {
        return true
    }
    return false
}

fun Fragment.openSideMenu() {
    activity?.takeIf { it is MainActivity }?.let {
        (it as MainActivity).openSideMenu()
    }
}

fun Fragment.closeSideMenu() {
    activity?.takeIf { it is MainActivity }?.let {
        (it as MainActivity).closeSideMenu()
    }
}

fun Fragment.toggleSideMenu() {
    activity?.takeIf { it is MainActivity }?.let {
        (it as MainActivity).toggleSideMenu()
    }
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

@ColorInt fun View.getBackgroundColor() : Int {
    var color: Int = Color.WHITE
    val background: Drawable? = background
    if (background is ColorDrawable)
        color = background.color
    return color
}

fun CharSequence.isValidEmail() : Boolean {
    return !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun isDeviceOnline(): Boolean {
    val cm = App.appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    if (Build.VERSION.SDK_INT < 23) {
        cm?.activeNetworkInfo?.let {
            return it.isConnected
        }
        return false

    } else {
        cm?.activeNetwork?.let {
            cm.getNetworkCapabilities(it)?.let { networkCapabilities ->
                return if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    true
            //                    } else if (
            //                    //Just for FortiClient VPN
            //                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED) &&
            //                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED) &&
            //                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            //                    ) {
            //
            //                        return true
            //
            //                    }
                } else {
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                }

            }
        }
        return false
    }
}

val Fragment.isGooglePlayServicesAvailable: Boolean
    get() {
        try {
            activity?.let { ctx ->
                val status = GoogleApiAvailability.getInstance()
                    .isGooglePlayServicesAvailable(ctx)
                if (status == ConnectionResult.SUCCESS) {
                    return true
                }
            } ?: kotlin.run {
                return false
            }
        } catch (e: Exception) {
        }
        return false
    }

@SuppressLint("WrongConstant")
fun View.hideSoftKeyboard() {
    try {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    } catch (e: Exception) {
    }
}

fun <T> Throwable.getErrorHttpModel(type: Class<T>) : T? {
    return if(this is HttpException){
        val body = this.response()?.errorBody()
        if(body != null ){
            try {
                Gson().fromJson(body.string(),type)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    else {
        null
    }
}

fun Throwable.isUnauthorizedError() : Boolean {
    return if(this is HttpException) {
        this.code() == 401
    }
    else {
        false
    }
}

fun InvalidRequestResponse.getFirstMessage() : String? {
    invalidRequestData?.let { errors ->
        return when {
            errors.nameErrors?.isNotEmpty() == true -> {
                errors.nameErrors[0]
            }
            errors.emailErrors?.isNotEmpty() == true -> {
                errors.emailErrors[0]
            }
            errors.passwordErrors?.isNotEmpty() == true -> {
                errors.passwordErrors[0]
            }
            else -> {
                null
            }
        }
    } ?: kotlin.run {
        return null
    }
}