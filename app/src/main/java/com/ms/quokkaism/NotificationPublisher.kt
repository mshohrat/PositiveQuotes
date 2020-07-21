package com.ms.quokkaism

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.ms.quokkaism.db.AppDatabase
import com.ms.quokkaism.model.ProfileSetting
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NotificationPublisher : BroadcastReceiver() {

    companion object {
        const val INTENT_REQUEST_CODE = 1001
        const val NOTIFICATION_CHANNEL_ID = "quokka schedule notification channel id"
        const val NOTIFICATION_CHANNEL_NAME = "Quokka schedule quotes"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Hawk.init(context).build()
        if (Hawk.contains(ProfileSetting.PROFILE_SETTING_KEY)) {
            Hawk.get<ProfileSetting>(ProfileSetting.PROFILE_SETTING_KEY)?.let {
                reScheduleNotification(context, it)
            } ?: kotlin.run {
                Hawk.put(ProfileSetting.NOTIFICATIONS_ARE_SET_KEY, false)
            }
        }
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                val lastUnreadQuote =
                    AppDatabase.getAppDataBase()?.quoteDao()?.getFirstUnreadQuote()
                lastUnreadQuote?.let { quote ->
                    val notificationManager = NotificationManagerCompat.from(context)
                    handleNotificationChannel(notificationManager)

                    val notificationBuilder =
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                            NotificationCompat.Builder(context)
                        } else {
                            NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        }
                    notificationBuilder
                        .setStyle(
                            NotificationCompat.BigTextStyle().bigText(
                                quote.text
                            )
                                .setBigContentTitle(context.getString(R.string.app_name))
                        )
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setShowWhen(true)
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

                    val resultIntent = Intent(context, MainActivity::class.java)
                    resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
                    stackBuilder.addParentStack(MainActivity::class.java)
                    stackBuilder.addNextIntent(resultIntent)

                    val resultPendingIntent = stackBuilder.getPendingIntent(
                        0, PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    notificationBuilder.setContentIntent(resultPendingIntent)

                    notificationManager.notify(
                        Math.random().times(1000).toInt(),
                        notificationBuilder.build()
                    )

                    quote.id?.let {
                        AppDatabase.getAppDataBase()?.quoteDao()?.updateQuoteIsRead(it, 1)
                    }
                }
            }
        }
    }

    private fun handleNotificationChannel(notificationManager: NotificationManagerCompat) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = notificationManager.notificationChannels.takeIf { it.isNotEmpty()}
            ?.filter { it.id == NOTIFICATION_CHANNEL_ID }
            ?.takeIf { it.isNotEmpty() }?.first()

        channel?.let {
            return
        } ?: kotlin.run {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                    .build())
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun reScheduleNotification(context: Context,profileSetting: ProfileSetting) {

        val intent = Intent(App.appContext,NotificationPublisher::class.java)
        val pendingIntent = PendingIntent.getBroadcast(App.appContext,
            INTENT_REQUEST_CODE,intent,
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
