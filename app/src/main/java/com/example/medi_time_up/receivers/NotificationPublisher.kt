package com.example.medi_time_up.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.medi_time_up.MainActivity
import com.example.medi_time_up.R

class NotificationPublisher : BroadcastReceiver() {

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TEXT = "extra_text"
        const val CHANNEL_ID = "meditime_channel"
        private const val TAG = "NotificationPublisher"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Recordatorio"
        val text = intent.getStringExtra(EXTRA_TEXT) ?: "Es hora de tu dosis"

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // create channel if needed (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                vibrationPattern = longArrayOf(0, 250, 200, 250)
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }

        // Intent para abrir la app cuando el usuario toque la notificación
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Sound
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Buscar drawable ic_notification en la app; si no existe, usar icono del sistema
        val iconRes = context.resources.getIdentifier("ic_notification", "drawable", context.packageName)
            .takeIf { it != 0 } ?: android.R.drawable.ic_dialog_info

        // Construir notificación
        val notif = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setContentTitle(title)
            setContentText(text)
            setSmallIcon(iconRes)
            setAutoCancel(true)
            setContentIntent(pi)
            setSound(soundUri)
            setVibrate(longArrayOf(0, 300, 200, 300))
            priority = NotificationCompat.PRIORITY_HIGH
        }.build()

        // Mostrar notificación
        try {
            nm.notify(System.currentTimeMillis().toInt(), notif)
            Log.d(TAG, "Notification posted: $title / $text")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to post notification", t)
        }
    }
}