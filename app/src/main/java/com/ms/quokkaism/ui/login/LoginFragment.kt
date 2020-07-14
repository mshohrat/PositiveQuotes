package com.ms.quokkaism.ui.login

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.ms.quokkaism.App
import com.ms.quokkaism.R
import com.ms.quokkaism.extension.*
import com.ms.quokkaism.model.Profile
import com.ms.quokkaism.ui.base.BaseFragment
import com.ms.quokkaism.util.LoadingDialog

import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : BaseFragment() {

    private lateinit var viewModel: LoginViewModel
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        subscribeToViewModel()
        subscribeToViewEvents()
    }

    private fun subscribeToViewModel() {
        viewModel.login.observe(viewLifecycleOwner, Observer {
            dismissDialog(loadingDialog)
            it?.let {
                val profile = Profile(
                    it.name,
                    it.email,
                    token = it.token,
                    refreshToken = it.refreshToken,
                    expiresIn = it.expiresIn,
                    isGuest = it.isGuest,
                    uuid = it.uuid
                )
                Hawk.put(Profile.PROFILE_KEY, profile)
                App.doRestart()
            }
        })
        viewModel.login_error.observe(viewLifecycleOwner, Observer {
            dismissDialog(loadingDialog)
            Toast.makeText(activity,it?.message,Toast.LENGTH_LONG).show()
        })
    }

    private fun subscribeToViewEvents() {
        login_btn?.setOnClickListener {
            when {
                login_email_et?.text?.toString()?.isEmpty() ?: true -> Toast.makeText(activity,R.string.enter_your_email,Toast.LENGTH_LONG).show()
                login_email_et?.text?.toString()?.isValidEmail()?.not() ?: false -> Toast.makeText(activity,R.string.entered_email_is_invalid,Toast.LENGTH_LONG).show()
                login_password_et?.text?.toString()?.isEmpty() ?: true -> Toast.makeText(activity,R.string.enter_your_password,Toast.LENGTH_LONG).show()
                else -> {
                    login_email_et?.text?.toString()?.let { email ->
                        login_password_et?.text?.toString()?.let { password ->
                            loadingDialog = showLoadingDialog()
                            viewModel.login(email,password)
                        }
                    }
                }
            }
        }

        login_back_btn?.setOnClickListener {
            activity?.onBackPressed()
        }

        login_not_registered_btn?.setOnClickListener {
            navigate(R.id.login_to_signup)
        }
    }

    override fun onDestroyView() {
        dismissDialog(loadingDialog)
        login_email_et?.hideSoftKeyboard()
        super.onDestroyView()
    }

}
