package com.speedroid.macroid

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.speedroid.macroid.ui.activity.MainActivity


class NotificationController(private val context: Context) {
    fun initializeNotification(): Notification {
        // create notification channel
        val notificationChannel = NotificationChannel(Configs.NOTIFICATION_CHANNEL_ID, Configs.NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(notificationChannel)

        // initialize notification click intent
        val clickIntent = Intent(context, MainActivity::class.java)
        clickIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP // prevent recreate instance(activity, fragment)
        val clickPendingIntent = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT) // update only if already exist

        // initialize notification builder
        val builder = NotificationCompat.Builder(context, Configs.NOTIFICATION_CHANNEL_ID)
        builder
            .setDefaults(Notification.DEFAULT_SOUND)
            .setVibrate(longArrayOf(0L)) // sound, vibrate, etc..
            .setAutoCancel(false) // touch remove
            .setOnlyAlertOnce(true) // prevent redundant
            .setOngoing(true) // persistence
            .setShowWhen(false) // push time
            .setSmallIcon(R.drawable.ic_notification) // small icon
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(clickPendingIntent)

        // build notification
        return builder.build()
    }
}
