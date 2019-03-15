package com.kuzheevadel.photogallery

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.SystemClock
import android.util.Log
import com.kuzheevadel.photogallery.utils.checkForNewPhoto

class PollService: IntentService(TAG) {

    override fun onHandleIntent(intent: Intent?) {
        if (!isNetworkingAvailable()) {
            return
        }

        Log.i(TAG, "PollService started")
        checkForNewPhoto(resources, applicationContext)
    }

    private fun isNetworkingAvailable(): Boolean {
        val cm: ConnectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isNetworkAvailable = cm.activeNetworkInfo != null

        return cm.activeNetworkInfo.isConnected && isNetworkAvailable
    }

    companion object {

        @JvmStatic
        val TAG = "PollService"

        @JvmStatic
        val POLL_INTERVAL_MS: Long = 60000

        @JvmStatic
        val channelId = "my_channel"

        @JvmStatic
        fun newIntent(context: Context): Intent {
            return Intent(context, PollService::class.java)
        }

        @JvmStatic
        fun setServiceAlarm(context: Context, isOn: Boolean) {
            val i = PollService.newIntent(context)
            val pi = PendingIntent.getService(context, 0, i, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (isOn) {
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), POLL_INTERVAL_MS, pi)
            } else {
                alarmManager.cancel(pi)
                pi.cancel()
            }
        }

        @JvmStatic
        fun isServiceAlarm(context: Context): Boolean {
            val i = PollService.newIntent(context)
            val pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE)
            return pi != null
        }
    }
}