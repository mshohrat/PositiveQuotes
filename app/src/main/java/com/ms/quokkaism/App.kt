package com.ms.quokkaism

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.orhanobut.hawk.Hawk
import kotlin.system.exitProcess

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Hawk.init(this.applicationContext).build()
        subscribeToFirebaseId()
        appContext = this
    }

    private fun subscribeToFirebaseId() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token


            })
    }

    companion object {

        private val TAG = "QUOKKA_APPLICATION"

        lateinit var appContext: Context

        fun doRestart() {
            val c = appContext
            try { //check if the appContext is given
                if (c != null) { //fetch the packagemanager so we can get the default launch activity
                    // (you can replace this intent with any other activity if you want
                    val pm = c.packageManager
                    //check if we got the PackageManager
                    if (pm != null) { //create the intent with the default start activity for your application
                        val mStartActivity = pm.getLaunchIntentForPackage(
                            c.packageName
                        )
                        if (mStartActivity != null) {
                            mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            //create a pending intent so the application is restarted after System.exit(0) was called.
                            // We use an AlarmManager to call this intent in 100ms
                            val mPendingIntentId = 223344
                            val mPendingIntent = PendingIntent
                                .getActivity(
                                    c, mPendingIntentId, mStartActivity,
                                    PendingIntent.FLAG_CANCEL_CURRENT
                                )
                            val mgr =
                                c.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                            mgr?.set(
                                AlarmManager.RTC,
                                System.currentTimeMillis() + 100,
                                mPendingIntent
                            )
                            //kill the application
                            exitProcess(0)
                        } else {
                            Log.e(
                                TAG,
                                "Was not able to restart application, mStartActivity null"
                            )
                        }
                    } else {
                        Log.e(
                            TAG,
                            "Was not able to restart application, PM null"
                        )
                    }
                } else {
                    Log.e(
                        TAG,
                        "Was not able to restart application, Context null"
                    )
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.e(
                    TAG,
                    "Was not able to restart application"
                )
            }
        }
    }
}