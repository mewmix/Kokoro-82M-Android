package com.example.kokoro82m.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import com.example.kokoro82m.MainActivity

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
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (playing) "Pause" else "Play",
                PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent(PlaybackReceiver.ACTION_TOGGLE),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                PendingIntent.getBroadcast(
                    context,
                    1,
                    Intent(PlaybackReceiver.ACTION_STOP),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setStyle(
                MediaNotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1)
            )
            .setOnlyAlertOnce(true)
            .build()

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
