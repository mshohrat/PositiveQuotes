package com.ms.quokkaism.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import com.ms.quokkaism.MainActivity

class CustomRecycler: RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context,attributeSet: AttributeSet?) : super(context,attributeSet)
    constructor(context: Context,attributeSet: AttributeSet?,defStyleAttr: Int) : super(context,attributeSet,defStyleAttr)

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return if(context is MainActivity && (context as MainActivity).isSideMenuOpened()){
            (context as MainActivity).closeSideMenu()
            true
        } else {
            super.onTouchEvent(e)
        }
    }
}