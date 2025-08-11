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

    fun show(context: Context, state: PlayerState) {
        createChannel(context)
        val notification = buildNotification(context, state)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun update(context: Context, state: PlayerState) {
        when (state) {
            PlayerState.PLAYING, PlayerState.PAUSED -> show(context, state)
            PlayerState.IDLE -> cancel(context)
        }
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun buildNotification(context: Context, state: PlayerState): android.app.Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Book Playback")
            .setOnlyAlertOnce(true)

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        when (state) {
            PlayerState.PLAYING -> {
                builder.setContentText("Playing")
                    .setOngoing(true)
                    .addAction(
                        android.R.drawable.ic_media_pause,
                        "Pause",
                        PendingIntent.getBroadcast(
                            context,
                            0,
                            Intent(PlaybackReceiver.ACTION_PAUSE),
                            flags
                        )
                    )
                    .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "Stop",
                        PendingIntent.getBroadcast(
                            context,
                            1,
                            Intent(PlaybackReceiver.ACTION_STOP),
                            flags
                        )
                    )
            }
            PlayerState.PAUSED -> {
                builder.setContentText("Paused")
                    .setOngoing(false)
                    .addAction(
                        android.R.drawable.ic_media_play,
                        "Play",
                        PendingIntent.getBroadcast(
                            context,
                            0,
                            Intent(PlaybackReceiver.ACTION_PLAY),
                            flags
                        )
                    )
                    .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "Stop",
                        PendingIntent.getBroadcast(
                            context,
                            1,
                            Intent(PlaybackReceiver.ACTION_STOP),
                            flags
                        )
                    )
            }
            else -> {
                builder.setContentText("Stopped").setOngoing(false)
            }
        }

        return builder.build()
    }

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
