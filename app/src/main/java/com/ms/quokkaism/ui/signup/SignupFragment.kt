package com.ms.quokkaism.ui.signup

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.ms.quokkaism.MainActivity
import com.ms.quokkaism.R
import com.ms.quokkaism.extension.*
import com.ms.quokkaism.util.LoadingDialog
import kotlinx.android.synthetic.main.fragment_signup.*

class SignupFragment : Fragment() {

    private lateinit var viewModel: SignupViewModel
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SignupViewModel::class.java)
        subscribeToViewModel()
        subscribeToViewEvents()
    }

    private fun subscribeToViewModel() {
        viewModel.signup.observe(viewLifecycleOwner, Observer {
            dismissDialog(loadingDialog)
            Toast.makeText(activity,it?.messageResId ?: 0, Toast.LENGTH_LONG).show()
            reloadSideMenu()
            navigate(R.id.signup_to_splash)
        })
        viewModel.signup_error.observe(viewLifecycleOwner, Observer {
            dismissDialog(loadingDialog)
            Toast.makeText(activity,it?.message, Toast.LENGTH_LONG).show()
        })
    }

    private fun subscribeToViewEvents() {
        signup_btn?.setOnClickListener {
            loadingDialog = showLoadingDialog()
            viewModel.signup(
                signup_name_et?.text?.toString(),
                signup_email_et?.text?.toString(),
                signup_password_et?.text?.toString(),
                signup_repeat_password_et?.text?.toString()
            )
        }
        signup_back_btn?.setOnClickListener {
            activity?.onBackPressed()
        }
        signup_have_account_btn?.setOnClickListener {
            navigateUp()
        }
    }

    override fun onDestroyView() {
        dismissDialog(loadingDialog)
        super.onDestroyView()
    }

}
