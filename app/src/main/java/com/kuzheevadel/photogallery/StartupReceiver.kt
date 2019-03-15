package com.kuzheevadel.photogallery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class StartupReceiver: BroadcastReceiver() {
    private val TAG = "StartUpReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {

        if (!QueryPreferences.isAlarmOn(context!!)) {
            PullServiceJobService.startJob(context, 1503201915)
        }
    }
}