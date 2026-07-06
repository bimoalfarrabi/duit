package com.duit.app.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.duit.app.R

object NotificationHelper {
    const val CHANNEL_ID = "duit_alerts"
    private var notifId = 0

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Peringatan Keuangan",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Budget hampir habis dan target tabungan tercapai" }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun send(context: Context, title: String, body: String) {
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(notifId++, notif)
    }
}
