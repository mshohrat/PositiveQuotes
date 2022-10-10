package com.ms.quokkaism.ui.profile

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.ms.quokkaism.R
import com.ms.quokkaism.extension.toggleSideMenu
import com.ms.quokkaism.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : BaseFragment() {

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        subscribeToViewModel()
        subscribeToViewEvents()
    }

    private fun subscribeToViewModel() {
        viewModel.profile.observe(viewLifecycleOwner, Observer {
            it?.let {
                it.name?.takeIf { it.isNotEmpty() }?.let { name ->
                    profile_name_tv?.text = name
                }
                it.email?.takeIf { it.isNotEmpty() }?.let { email ->
                    profile_email_tv?.text = email
                }
            }
        })
    }

    private fun subscribeToViewEvents() {
        profile_menu_btn?.setOnClickListener {
            toggleSideMenu()
        }
    }

}
