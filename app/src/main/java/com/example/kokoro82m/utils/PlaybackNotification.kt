package com.example.kokoro82m.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object PlaybackNotification {
    private const val CHANNEL_ID = "book_playback_channel"
    private const val NOTIFICATION_ID = 1001

    fun show(context: Context, playing: Boolean) {
        createChannel(context)
        val notification = buildNotification(context, playing)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun update(context: Context, state: PlayerState) {
        when (state) {
            PlayerState.PLAYING -> show(context, true)
            PlayerState.PAUSED -> show(context, false)
            PlayerState.IDLE -> cancel(context)
        }
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun buildNotification(context: Context, playing: Boolean) =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Book Playback")
            .setContentText(if (playing) "Playing" else "Paused")
            .setOngoing(playing)
            .apply {
                if (playing) {
                    addAction(
                        android.R.drawable.ic_media_pause,
                        "Pause",
                        broadcastIntent(context, PlaybackReceiver.ACTION_PAUSE)
                    )
                } else {
                    addAction(
                        android.R.drawable.ic_media_play,
                        "Play",
                        broadcastIntent(context, PlaybackReceiver.ACTION_PLAY)
                    )
                }
                addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Stop",
                    broadcastIntent(context, PlaybackReceiver.ACTION_STOP)
                )
            }
            .setOnlyAlertOnce(true)
            .build()

    private fun broadcastIntent(context: Context, action: String): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            Intent(context, PlaybackReceiver::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Book Playback",
                    NotificationManager.IMPORTANCE_LOW
                )
                manager.createNotificationChannel(channel)
            }
        }
    }
}
