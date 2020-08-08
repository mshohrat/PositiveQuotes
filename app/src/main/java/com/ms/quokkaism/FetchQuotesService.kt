package com.ms.quokkaism

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ms.quokkaism.NotificationPublisher.Companion.NOTIFICATION_CHANNEL_ID
import com.ms.quokkaism.db.AppDatabase
import com.ms.quokkaism.db.model.Quote
import com.ms.quokkaism.extension.isDeviceOnline
import com.ms.quokkaism.network.base.ApiServiceGenerator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FetchQuotesService : Service() {

    private val NOTIFICATION_ID = 987654

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        val notificationManager = NotificationManagerCompat.from(this)
        handleNotificationChannel(notificationManager)
        val notificationBuilder =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                NotificationCompat.Builder(this)
            } else {
                NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            }
        notificationBuilder
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    this.getString(R.string.fetching_data_from_server)
                )
                    .setBigContentTitle(this.getString(R.string.app_name))
            )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setShowWhen(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(null)
        startForeground(NOTIFICATION_ID,notificationBuilder.build())

        fetchQuotesFromServer()
    }

    @SuppressLint("CheckResult")
    private fun fetchQuotesFromServer() {
        if(isDeviceOnline()) {
            ApiServiceGenerator.getApiService.getQuotes()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({
                    it?.quoteList?.takeIf { it.isNotEmpty() }?.let {
                        val list = mutableListOf<Quote>()
                        it.forEach {
                            it.text?.let { text ->
                                list.add(
                                    Quote(
                                        it.id,
                                        text,
                                        it.author
                                    )
                                )
                            }
                        }
                        list.takeIf { it.isNotEmpty() }?.let {
                            GlobalScope.launch {
                                AppDatabase.getAppDataBase()?.quoteDao()?.insertQuotes(list)
                                stopForeground(true)
                                stopSelf()
                            }
                        } ?: kotlin.run {
                            stopForeground(true)
                            stopSelf()
                        }
                    } ?: kotlin.run {
                        stopForeground(true)
                        stopSelf()
                    }
                }, {
                    stopForeground(true)
                    stopSelf()
                })
        } else {
            stopForeground(true)
            stopSelf()
        }
    }

    private fun handleNotificationChannel(notificationManager: NotificationManagerCompat) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val channel = notificationManager.notificationChannels.takeIf { it.isNotEmpty()}
            ?.filter { it.id == NotificationPublisher.NOTIFICATION_CHANNEL_ID }
            ?.takeIf { it.isNotEmpty() }?.first()

        channel?.let {
            return
        } ?: kotlin.run {
            val notificationChannel = NotificationChannel(
                NotificationPublisher.NOTIFICATION_CHANNEL_ID, NotificationPublisher.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                    .build())
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}
