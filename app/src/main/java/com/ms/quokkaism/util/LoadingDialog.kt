package com.ms.quokkaism.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.annotation.StringRes
import com.google.android.material.textview.MaterialTextView
import com.ms.quokkaism.R

class LoadingDialog(context: Context,@StringRes private val messageResId : Int? = null): Dialog(context) {

    init {
        setContentView(R.layout.compound_view_loading_dialog)
        setCancelable(false)
        messageResId?.let {
            findViewById<MaterialTextView>(R.id.compound_view_loading_dialog_message)?.setText(it)
        }
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}