package com.ms.quokkaism.ui.setting

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.ms.quokkaism.App
import com.ms.quokkaism.NotificationPublisher
import com.ms.quokkaism.R
import com.ms.quokkaism.extension.convertDpToPixel
import com.ms.quokkaism.extension.dismissDialog
import com.ms.quokkaism.extension.showLoadingDialog
import com.ms.quokkaism.extension.toggleSideMenu
import com.ms.quokkaism.model.ProfileSetting
import com.ms.quokkaism.ui.base.BaseFragment
import com.ms.quokkaism.ui.setting.adapter.SettingTimeAdapter
import com.ms.quokkaism.util.GridSpacingItemDecoration
import com.ms.quokkaism.util.LoadingDialog
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.fragment_setting.*

class SettingFragment : BaseFragment(), SettingTimeAdapter.OnItemClickListener {

    private lateinit var viewModel: SettingViewModel
    private val settingRecyclerSpanCount = 3
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SettingViewModel::class.java)
        initRecycler()
        subscribeToViewModel()
        subscribeToViewEvents()
    }

    private fun initRecycler() {
        val settingTimeAdapter = SettingTimeAdapter(this,viewModel.setting.value?.interval)
        setting_time_rv?.layoutManager = GridLayoutManager(activity, settingRecyclerSpanCount)
        setting_time_rv?.adapter = settingTimeAdapter
        val spacing = convertDpToPixel(activity,24f).toInt()
        setting_time_rv?.addItemDecoration(GridSpacingItemDecoration(settingRecyclerSpanCount,spacing,true))
    }

    private fun subscribeToViewModel() {
        viewModel.isSettingChanged.observe(viewLifecycleOwner, Observer {
            if(it)
            {
                setting_save_btn?.visibility = View.VISIBLE
            }
            else
            {
                setting_save_btn?.visibility = View.GONE
            }
        })
        viewModel.setting.observe(viewLifecycleOwner, Observer {
            if(setting_save_btn?.visibility == View.VISIBLE) {
                Toast.makeText(activity,R.string.setting_changed_successfully,Toast.LENGTH_SHORT).show()
                dismissDialog(loadingDialog)
                it?.let { s ->
                    reScheduleNotification(s)
                }
            }
        })
        viewModel.settingError.observe(viewLifecycleOwner, Observer {
            it?.message?.takeIf { it.isNotEmpty() } ?.let {
                Toast.makeText(activity,it,Toast.LENGTH_SHORT).show()
            } ?: kotlin.run {
                Toast.makeText(activity,it?.messageResId ?: 0,Toast.LENGTH_SHORT).show()
            }
            dismissDialog(loadingDialog)
        })
    }

    private fun subscribeToViewEvents() {
        setting_menu_btn?.setOnClickListener {
            toggleSideMenu()
        }
        setting_save_btn?.setOnClickListener {
            loadingDialog = showLoadingDialog()
            viewModel.changeSetting()
        }
    }

    private fun reScheduleNotification(profileSetting: ProfileSetting) {

        val intent = Intent(App.appContext,NotificationPublisher::class.java)
        val pendingIntent = PendingIntent.getBroadcast(App.appContext,
            NotificationPublisher.INTENT_REQUEST_CODE,intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
        val futureInMillis = System.currentTimeMillis() + (profileSetting.interval.toLong() * 3600000)

        val alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        try {
            alarmManager?.cancel(pendingIntent)
        } catch (e : Exception) {

        }
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,futureInMillis,pendingIntent)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                alarmManager?.setExact(AlarmManager.RTC_WAKEUP,futureInMillis,pendingIntent)
            }
            else -> {
                alarmManager?.set(AlarmManager.RTC_WAKEUP,futureInMillis,pendingIntent)
            }
        }
        Hawk.put(ProfileSetting.NOTIFICATIONS_ARE_SET_KEY,true)
    }

    override fun onItemClick(position: Int, item: Int) {
        viewModel.settingInterval = item
    }

}
