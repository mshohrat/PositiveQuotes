package com.ms.quokkaism

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.ms.quokkaism.model.ProfileSetting
import com.orhanobut.hawk.Hawk

class BootReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            Hawk.init(context)
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

        val intent = Intent(context, NotificationPublisher::class.java)
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        val pendingIntent = PendingIntent.getBroadcast(context,
            NotificationPublisher.INTENT_REQUEST_CODE,intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
        val internalInMillis = profileSetting.interval.times(60).times(60).times(1000)
        val futureInMillis = System.currentTimeMillis() + internalInMillis

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if(Hawk.contains(ProfileSetting.NOTIFICATIONS_ARE_SET_KEY) && Hawk.get<Boolean>(
                ProfileSetting.NOTIFICATIONS_ARE_SET_KEY) == true)
        {
            alarmManager?.cancel(pendingIntent)
        }
        alarmManager?.setRepeating(AlarmManager.RTC_WAKEUP,futureInMillis,internalInMillis.toLong(),pendingIntent)
        Hawk.put(ProfileSetting.NOTIFICATIONS_ARE_SET_KEY,true)
    }
}