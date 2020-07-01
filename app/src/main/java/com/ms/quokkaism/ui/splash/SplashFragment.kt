package com.ms.quokkaism.ui.splash

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.iid.FirebaseInstanceId

import com.ms.quokkaism.R
import com.ms.quokkaism.extension.isUserLoggedIn
import com.ms.quokkaism.extension.navigate
import com.ms.quokkaism.model.Profile
import com.ms.quokkaism.model.ProfileSetting
import com.ms.quokkaism.network.base.ApiServiceGenerator
import com.ms.quokkaism.network.model.RegisterFbTokenRequest
import com.ms.quokkaism.ui.base.BaseFragment
import com.orhanobut.hawk.Hawk
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_splash.*

class SplashFragment : BaseFragment() {

    private lateinit var viewModel: SplashViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SplashViewModel::class.java)
        subscribeToViewModel()
        subscribeToViewEvents()
        if(isUserLoggedIn())
        {
            viewModel.refreshConfig()
        }
        else
        {
            viewModel.loginAsGuest()
        }
    }

    private fun subscribeToViewModel() {
        viewModel.config.observe(viewLifecycleOwner, Observer {
            it?.takeIf { it.isUserActive }?.let {
                it.setting?.interval?.let { interval ->
                    Hawk.put(ProfileSetting.PROFILE_SETTING_KEY,ProfileSetting(interval))
                }
                if(it.requiresFbToken)
                {
                    sendFbTokenToServerAndNavigateToMain();
                }
                else {
                    navigateToHome()
                }
            } ?: kotlin.run {
                showErrorToUser()
                hideLoading()
                showRetry()
            }
        })
        viewModel.config_error.observe(viewLifecycleOwner, Observer {
            showErrorToUser(it?.message)
            hideLoading()
            showRetry()
        })
        viewModel.login_as_guest.observe(viewLifecycleOwner, Observer {
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
                Hawk.put(Profile.PROFILE_KEY,profile)
                viewModel.refreshConfig()
            } ?: kotlin.run {
                showErrorToUser()
                hideLoading()
                showRetry()
            }
        })
        viewModel.login_as_guest_error.observe(viewLifecycleOwner, Observer {
            showErrorToUser(it?.message)
            hideLoading()
            showRetry()
        })
    }

    private fun sendFbTokenToServerAndNavigateToMain() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener{
            if(it.isComplete && it.isSuccessful)
            {
                it.result?.token?.takeIf { it.isNotEmpty() }?.let { token ->
                    ApiServiceGenerator.getApiService.registerFbToken(RegisterFbTokenRequest(token))
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe ({

                        },{

                        })
                }
            }
            navigateToHome()

        }).addOnFailureListener(OnFailureListener {
            navigateToHome()
        })
    }

    private fun navigateToHome()
    {
        navigate(R.id.splash_to_home)
        val graph = findNavController().graph
        graph.startDestination = R.id.nav_home
        findNavController().graph = graph
    }

    private fun subscribeToViewEvents() {
        splash_retry_btn?.setOnClickListener {
            hideRetry()
            showLoading()
            if(isUserLoggedIn())
            {
                viewModel.refreshConfig()
            }
            else
            {
                viewModel.loginAsGuest()
            }
        }
    }

    private fun showRetry() {
        splash_retry_btn?.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        splash_progress?.visibility = View.GONE
    }

    private fun hideRetry() {
        splash_retry_btn?.visibility = View.GONE
    }

    private fun showLoading() {
        splash_progress?.visibility = View.VISIBLE
    }

    private fun showErrorToUser(message: String? = null) {
        message?.let {
            Toast.makeText(activity,message,Toast.LENGTH_SHORT)
        } ?: kotlin.run {
            Toast.makeText(activity,R.string.error_on_connecting_to_server,Toast.LENGTH_SHORT)
        }
    }

}
