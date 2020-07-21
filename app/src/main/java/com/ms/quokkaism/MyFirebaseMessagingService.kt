package com.ms.quokkaism

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.ms.quokkaism.db.AppDatabase
import com.ms.quokkaism.db.model.Quote
import com.ms.quokkaism.model.ProfileSetting
import com.ms.quokkaism.network.base.ApiServiceGenerator
import com.ms.quokkaism.network.model.ReceivedNotificationData
import com.ms.quokkaism.network.model.ReceivedQuote
import com.ms.quokkaism.network.model.RegisterFbTokenRequest
import com.orhanobut.hawk.Hawk
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.toString().let {
            val data = Gson().fromJson<ReceivedNotificationData>(it, ReceivedNotificationData::class.java)
            data?.quotes?.takeIf { it.isNotEmpty() }?.let { quotes ->
                val finalList = mutableListOf<Quote>()
                for (quote in quotes) {
                    quote?.text?.let {
                        finalList.add(
                            Quote(
                                id = quote.id,
                                text = it,
                                author = quote.author
                            )
                        )
                    }
                }
                finalList.takeIf { it.isNotEmpty() }?.let {
                    GlobalScope.launch {
                        AppDatabase.getAppDataBase()?.quoteDao()?.insertQuotes(it.toList())
                    }
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        token.let {
            ApiServiceGenerator.getApiService.registerFbToken(RegisterFbTokenRequest(it))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe ({},{})
        }
    }

    private fun scheduleNotification() {
        Hawk.init(this).build()
        if(Hawk.contains(ProfileSetting.NOTIFICATIONS_ARE_SET_KEY) && Hawk.get<Boolean>(
                ProfileSetting.NOTIFICATIONS_ARE_SET_KEY) == true)
        {
            return
        }
        if(Hawk.contains(ProfileSetting.PROFILE_SETTING_KEY))
        {
            val setting = Hawk.get<ProfileSetting?>(ProfileSetting.PROFILE_SETTING_KEY)
            setting?.let {
                val intent = Intent(App.appContext,NotificationPublisher::class.java)
                val pendingIntent = PendingIntent.getBroadcast(App.appContext,NotificationPublisher.INTENT_REQUEST_CODE,intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
                val futureInMillis = System.currentTimeMillis() + (it.interval.toLong() * 3600000)
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
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
    }
}