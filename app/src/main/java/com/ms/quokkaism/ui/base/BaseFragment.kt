package com.ms.quokkaism.ui.base

import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ms.quokkaism.MainActivity
import com.ms.quokkaism.extension.getBackgroundColor

abstract class BaseFragment : Fragment(), OnBackPressedListener
{
    override fun onBackPress(): Boolean {
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //setBackgroundColorInActivity()
    }

    protected fun setBackgroundColorInActivity()
    {
        view?.takeIf { it is ViewGroup }?.let { view ->
            activity?.takeIf { it is MainActivity }?.let {
                (it as MainActivity).setMainBackgroundColor(view.getBackgroundColor())
                view.setBackgroundResource(android.R.color.transparent)
            }
        }
    }
}

interface OnBackPressedListener
{
    fun onBackPress() : Boolean
}