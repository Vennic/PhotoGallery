package com.kuzheevadel.photogallery.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.kuzheevadel.photogallery.*
import com.kuzheevadel.photogallery.web.FlicrDownload
import com.kuzheevadel.photogallery.web.getPhotos

fun notify(resources: Resources,
           context: Context,
           items: ArrayList<GalleryItem>,
           lastResultId: String?) {

    if (items.isEmpty()) {
        return
    }

    val resultId = items[0].id

    if (resultId == lastResultId) {
        Log.i(PollService.TAG, "Got an old result: $resultId")
    } else {
        Log.i(PollService.TAG, "Got a new resultID: $resultId")


        val i: Intent = PhotoGalleryActivity.newIntent(context)
        val pi: PendingIntent = PendingIntent.getActivity(context, 0, i, 0)

        val notificationChannel: NotificationChannel? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(PollService.channelId, "Adel", NotificationManager.IMPORTANCE_HIGH)
        } else {
            null
        }

        val notification: Notification = NotificationCompat.Builder(context, PollService.channelId)
                .setTicker(resources.getString(R.string.new_picture_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_picture_title))
                .setContentText(resources.getString(R.string.new_picture_text))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build()

        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(notificationChannel!!)
        }
        notificationManager.notify(1, notification)
    }

    QueryPreferences.setLastResultId(context, resultId)

}

fun checkForNewPhoto(resources: Resources, context: Context) {
    val query = QueryPreferences.getStoredQuery(context)
    val lastResultId = QueryPreferences.getLastResultId(context)
    var items: List<GalleryItem> = ArrayList()

    when(query) {
        null -> getPhotos(1, query, 1, object : FlicrDownload {
            override fun onDownloaded(list: ArrayList<GalleryItem>) {
                items = list
                notify(resources, context, list, lastResultId)
                Log.i(PollService.TAG, "query null: ${list.size}")
            }
        })
        else -> getPhotos(2, query, 1, object : FlicrDownload {
            override fun onDownloaded(list: ArrayList<GalleryItem>) {
                items = list
                notify(resources, context, list, lastResultId)
                Log.i(PollService.TAG, "query else: ${list.size}")
            }
        })
    }

    if (items.isEmpty()) {
        return
    }
}