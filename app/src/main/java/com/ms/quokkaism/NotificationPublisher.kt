package com.ms.quokkaism

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ms.quokkaism.db.AppDatabase

class NotificationPublisher : BroadcastReceiver() {

    companion object {
        const val INTENT_REQUEST_CODE = 1001
        const val NOTIFICATION_CHANNEL_ID = "quokka schedule notification channel id"
        const val NOTIFICATION_CHANNEL_NAME = "Quokka schedule quotes"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val quoteLiveData = AppDatabase.getAppDataBase()?.quoteDao()?.getLastReadQuote()
        quoteLiveData?.value?.let { quote ->

            val notificationManager = NotificationManagerCompat.from(context)
            handleNotificationChannel(notificationManager)
            val notification = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                NotificationCompat.Builder(context)
                    .setColor(context.resources.getColor(R.color.peach_orange))
                    .setContentText(quote.text)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setShowWhen(true)
                    .setAutoCancel(true)
                    .setSubText(quote.author)
                    .build()
            } else {
                NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_NAME)
                    .setColor(context.getColor(R.color.peach_orange))
                    .setShowWhen(true)
                    .setContentText(quote.text)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setAutoCancel(true)
                    .setSubText(quote.author)
                    .build()
            }
            notificationManager.notify(Math.random().times(1000).toInt(),notification)
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
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}
