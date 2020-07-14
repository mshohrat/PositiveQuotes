package com.ms.quokkaism

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.ms.quokkaism.db.AppDatabase
import com.ms.quokkaism.db.model.Quote
import com.ms.quokkaism.network.base.ApiServiceGenerator
import com.ms.quokkaism.network.model.ReceivedNotificationData
import com.ms.quokkaism.network.model.ReceivedQuote
import com.ms.quokkaism.network.model.RegisterFbTokenRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data?.toString()?.let {
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

    override fun onNewToken(token: String?) {
        token?.let {
            ApiServiceGenerator.getApiService.registerFbToken(RegisterFbTokenRequest(it))
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe ({},{})
        }
    }
}