package com.ms.quokkaism.ui.sidemenu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ms.quokkaism.MainActivity

import com.ms.quokkaism.R
import com.ms.quokkaism.extension.closeSideMenu
import com.ms.quokkaism.extension.navigate
import com.ms.quokkaism.extension.navigateUp
import com.ms.quokkaism.model.Profile
import com.ms.quokkaism.ui.sidemenu.adapter.SideMenuAdapter
import kotlinx.android.synthetic.main.fragment_side_menu.*

/**
 * A simple [Fragment] subclass.
 */
class SideMenuFragment : Fragment(), SideMenuAdapter.OnItemClickListener, NavController.OnDestinationChangedListener {

    private lateinit var sideMenuViewModel: SideMenuViewModel
    private lateinit var sideMenuAdapter: SideMenuAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_side_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sideMenuViewModel = ViewModelProviders.of(this).get(SideMenuViewModel::class.java)
        sideMenuViewModel.profile.observe(viewLifecycleOwner, Observer {
            side_menu_name_tv?.text = it?.name
            if(Profile.isUserGuest())
            {
                side_menu_view_profile_btn?.setText(R.string.login_signup)
            }
            else
            {
                side_menu_view_profile_btn?.setText(R.string.view_my_profile)
            }
        })
        initRecycler()
        subscribeToNavigationChanges()
        subscribeToViewEvents()
    }

    private fun initRecycler() {
        sideMenuAdapter = SideMenuAdapter(this)
        side_menu_recycler?.layoutManager = LinearLayoutManager(activity,RecyclerView.VERTICAL,false)
        side_menu_recycler?.adapter = sideMenuAdapter
    }

    private fun subscribeToNavigationChanges() {
        activity?.takeIf { it is MainActivity }?.let {
            (it as MainActivity).getNavController().addOnDestinationChangedListener(this)
        }
    }

    private fun subscribeToViewEvents() {
        side_menu_view_profile_btn?.setOnClickListener {
            onItemClick(-1,R.string.profile)
        }
    }

    override fun onItemClick(position: Int, @StringRes item: Int) {
        activity?.takeIf { it is MainActivity }?.let {
            val navController = (it as MainActivity).getNavController()
            when(navController.currentDestination?.id)
            {
                R.id.nav_home -> {
                    when(item){
                        R.string.setting -> {navigate(R.id.home_to_setting); closeSideMenu();}
                        R.string.likes -> {navigate(R.id.home_to_likes); closeSideMenu();}
                        R.string.about_us -> {navigate(R.id.home_to_about); closeSideMenu();}
                        R.string.profile -> {
                            if(Profile.isUserGuest())
                            {
                                navigate(R.id.home_to_login)
                                closeSideMenu()
                            }
                            else {
                                navigate(R.id.home_to_profile)
                                closeSideMenu()
                            }
                        }
                        else -> {}
                    }
                }
                R.id.nav_profile -> {
                    when(item){
                        R.string.setting -> {navigate(R.id.profile_to_setting); closeSideMenu();}
                        R.string.likes -> {navigate(R.id.profile_to_likes); closeSideMenu();}
                        R.string.about_us -> {navigate(R.id.profile_to_about); closeSideMenu();}
                        R.string.home -> { navigateUp(); closeSideMenu();}
                        else -> {}
                    }
                }
                R.id.nav_setting -> {
                    when(item){
                        R.string.home -> {navigateUp(); closeSideMenu();}
                        R.string.likes -> {navigate(R.id.setting_to_likes); closeSideMenu();}
                        R.string.about_us -> {navigate(R.id.setting_to_about); closeSideMenu();}
                        R.string.profile -> {
                            if(Profile.isUserGuest())
                            {
                                navigate(R.id.setting_to_login)
                                closeSideMenu()
                            }
                            else {
                                navigate(R.id.setting_to_profile)
                                closeSideMenu()
                            }
                        }
                        else -> {}
                    }
                }
                R.id.nav_likes -> {
                    when(item){
                        R.string.home -> {navigateUp(); closeSideMenu();}
                        R.string.setting -> {navigate(R.id.likes_to_setting); closeSideMenu();}
                        R.string.about_us -> {navigate(R.id.likes_to_about); closeSideMenu();}
                        R.string.profile -> {
                            if(Profile.isUserGuest())
                            {
                                navigate(R.id.likes_to_login)
                                closeSideMenu()
                            }
                            else {
                                navigate(R.id.likes_to_profile)
                                closeSideMenu()
                            }
                        }
                        else -> {}
                    }
                }
                else -> {}
            }
        }

    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        when(destination.id) {
            R.id.nav_home -> sideMenuAdapter.selectItem(R.string.home)
//            R.id.nav_setting -> sideMenuAdapter.selectItem(R.string.setting)
//            R.id.nav_likes -> sideMenuAdapter.selectItem(R.string.likes)
//            R.id.nav_about -> sideMenuAdapter.selectItem(R.string.about_us)
            R.id.nav_profile -> sideMenuAdapter.selectItem(-1)
            else -> {/* sideMenuAdapter.selectItem(-1)*/ }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.takeIf { it.isFinishing.not() && it is MainActivity }?.let {
            (it as? MainActivity)?.getNavController()?.removeOnDestinationChangedListener(this)
        }
    }

}
