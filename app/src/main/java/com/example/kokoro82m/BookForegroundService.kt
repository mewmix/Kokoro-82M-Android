package com.example.kokoro82m

import ai.onnxruntime.OrtSession
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import com.example.kokoro82m.utils.InterpolationMode
import com.example.kokoro82m.utils.PhonemeConverter
import com.example.kokoro82m.utils.PlayerState
import com.example.kokoro82m.utils.StyleLoader
import com.example.kokoro82m.viewmodel.BookViewModel

class BookForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START, ACTION_PLAY -> {
                val start = if (intent.action == ACTION_START) startLine
                else bookViewModel?.currentLine?.value?.coerceAtLeast(0) ?: 0
                startForeground(NOTIFICATION_ID, buildNotification(PlayerState.PLAYING))
                bookViewModel?.startPlayback(
                    session = session ?: return START_NOT_STICKY,
                    phonemeConverter = phonemeConverter ?: return START_NOT_STICKY,
                    styleLoader = styleLoader ?: return START_NOT_STICKY,
                    selectedStyles = selectedStyles,
                    weights = weights,
                    mode = mode,
                    speed = speed,
                    lines = lines,
                    startLine = start,
                    bookUri = bookUri,
                    context = this,
                    usePregenerated = usePregenerated,
                    onFinished = {
                        stopForeground(true)
                        stopSelf()
                        onFinished?.invoke()
                    },
                )
            }
            ACTION_PAUSE -> {
                bookViewModel?.audioPlayer?.pause()
                updateNotification(PlayerState.PAUSED)
            }
            ACTION_STOP -> {
                bookViewModel?.stopPlayback()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun updateNotification(state: PlayerState) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(state))
    }

    private fun buildNotification(state: PlayerState): Notification {
        createChannel()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Book Playback")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOnlyAlertOnce(true)
            .setStyle(MediaNotificationCompat.MediaStyle())

        if (state == PlayerState.PLAYING) {
            builder.addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_media_pause,
                    "Pause",
                    servicePendingIntent(ACTION_PAUSE)
                )
            )
        } else {
            builder.addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_media_play,
                    "Play",
                    servicePendingIntent(ACTION_PLAY)
                )
            )
        }
        builder.addAction(
            NotificationCompat.Action(
                android.R.drawable.ic_media_stop,
                "Stop",
                servicePendingIntent(ACTION_STOP)
            )
        )
        return builder.build()
    }

    private fun servicePendingIntent(action: String): PendingIntent {
        val intent = Intent(this, BookForegroundService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Book Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "book_playback_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.example.kokoro82m.action.START"
        private const val ACTION_PLAY = "com.example.kokoro82m.action.PLAY"
        private const val ACTION_PAUSE = "com.example.kokoro82m.action.PAUSE"
        private const val ACTION_STOP = "com.example.kokoro82m.action.STOP"

        private var session: OrtSession? = null
        private var phonemeConverter: PhonemeConverter? = null
        private var styleLoader: StyleLoader? = null
        private var selectedStyles: List<String> = emptyList()
        private var weights: Map<String, Float> = emptyMap()
        private var mode: InterpolationMode = InterpolationMode.LINEAR
        private var speed: Float = 1f
        private var lines: List<String> = emptyList()
        private var startLine: Int = 0
        private var bookUri: Uri? = null
        private var usePregenerated: Boolean = false
        private var bookViewModel: BookViewModel? = null
        private var onFinished: (() -> Unit)? = null

        fun startService(
            context: Context,
            bookViewModel: BookViewModel,
            session: OrtSession,
            phonemeConverter: PhonemeConverter,
            styleLoader: StyleLoader,
            selectedStyles: List<String>,
            weights: Map<String, Float>,
            mode: InterpolationMode,
            speed: Float,
            lines: List<String>,
            startLine: Int,
            bookUri: Uri?,
            usePregenerated: Boolean,
            onFinished: () -> Unit,
        ) {
            this.bookViewModel = bookViewModel
            this.session = session
            this.phonemeConverter = phonemeConverter
            this.styleLoader = styleLoader
            this.selectedStyles = selectedStyles
            this.weights = weights
            this.mode = mode
            this.speed = speed
            this.lines = lines
            this.startLine = startLine
            this.bookUri = bookUri
            this.usePregenerated = usePregenerated
            this.onFinished = onFinished

            val intent = Intent(context, BookForegroundService::class.java).apply {
                action = ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun pause(context: Context) {
            val intent = Intent(context, BookForegroundService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, BookForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}

