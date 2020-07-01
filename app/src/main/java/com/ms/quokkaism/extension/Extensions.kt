package com.ms.quokkaism.extension

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Patterns
import android.view.View
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ms.quokkaism.MainActivity
import com.ms.quokkaism.model.Profile
import com.ms.quokkaism.util.LoadingDialog
import com.orhanobut.hawk.Hawk


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

fun Fragment.showLoadingDialog() : LoadingDialog? {
    activity?.takeIf { it.isFinishing.not() }?.let {
        val dialog = LoadingDialog(it)
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