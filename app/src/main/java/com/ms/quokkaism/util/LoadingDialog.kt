package com.ms.quokkaism.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.ms.quokkaism.R

class LoadingDialog(context: Context): Dialog(context) {

    init {
        setContentView(R.layout.compound_view_loading_dialog)
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}