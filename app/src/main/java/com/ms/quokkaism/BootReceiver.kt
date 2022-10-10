package com.ms.quokkaism

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.ms.quokkaism.model.ProfileSetting
import com.orhanobut.hawk.Hawk

class BootReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            Hawk.init(context).build()
            Toast.makeText(context, "Yex", Toast.LENGTH_SHORT).show()
            if (Hawk.contains(ProfileSetting.PROFILE_SETTING_KEY)) {
                reScheduleNotification(
                    context,
                    Hawk.get<ProfileSetting>(ProfileSetting.PROFILE_SETTING_KEY)
                )
            }
        }
    }

    private fun reScheduleNotification(context: Context,profileSetting: ProfileSetting) {

        val intent = Intent(App.appContext,NotificationPublisher::class.java)
        val pendingIntent = PendingIntent.getBroadcast(App.appContext,
            NotificationPublisher.INTENT_REQUEST_CODE,intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
        val futureInMillis = System.currentTimeMillis() + (profileSetting.interval.toLong() * 3600000)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
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
}