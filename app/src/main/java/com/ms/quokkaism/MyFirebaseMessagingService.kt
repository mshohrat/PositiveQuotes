package com.ms.quokkaism

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.ms.quokkaism.db.AppDatabase
import com.ms.quokkaism.db.Quote
import com.ms.quokkaism.network.base.ApiServiceGenerator
import com.ms.quokkaism.network.model.RegisterFbTokenRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data?.takeIf { it.containsKey("quotes") }?.let {
            val quotes = Gson().fromJson<List<Quote?>>(it["quotes"],Quote::class.java)
            for (quote in quotes)
            {
                quote?.let {
                    AppDatabase.getAppDataBase()?.quoteDao()?.insertQuote(
                        Quote(text = it.text,author = it.author)
                    )
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