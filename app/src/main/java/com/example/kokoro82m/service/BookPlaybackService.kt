package com.example.kokoro82m.service

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
import com.example.kokoro82m.R
import com.example.kokoro82m.utils.AudioPlayer
import com.example.kokoro82m.utils.Bookmark
import com.example.kokoro82m.utils.BookmarkManager
import com.example.kokoro82m.utils.InterpolationMode
import com.example.kokoro82m.utils.PlayerState
import com.example.kokoro82m.utils.TtsManager
import com.example.kokoro82m.utils.playBook
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class BookPlaybackService : Service() {
    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val audioPlayer = AudioPlayer(scope) { updateNotification() }
    private var playJob: Job? = null
    private var currentLine: Int = 0
    private var bookUri: String? = null
    private var bookmark: Bookmark? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> {
                audioPlayer.pause()
                bookmark = Bookmark(currentLine, audioPlayer.getPosition())
                bookUri?.let { BookmarkManager.save(this, it, bookmark!!.line, bookmark!!.position) }
                updateNotification()
            }
            ACTION_RESUME -> {
                audioPlayer.play()
                updateNotification()
            }
            ACTION_STOP -> {
                bookmark = Bookmark(currentLine, audioPlayer.getPosition())
                bookUri?.let { BookmarkManager.save(this, it, bookmark!!.line, bookmark!!.position) }
                stopPlayback()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> startPlayback(intent)
        }
        return START_STICKY
    }

    private fun startPlayback(intent: Intent?) {
        if (playJob != null) return
        val lines = intent?.getStringArrayListExtra(EXTRA_LINES) ?: return
        currentLine = intent.getIntExtra(EXTRA_START_LINE, 0)
        bookUri = intent.getStringExtra(EXTRA_BOOK_URI)
        val selectedStyles = intent.getStringArrayListExtra(EXTRA_SELECTED_STYLES) ?: listOf("af_sarah")
        val weights = intent.getSerializableExtra(EXTRA_WEIGHTS) as? HashMap<String, Float> ?: mapOf("af_sarah" to 1f)
        val mode = InterpolationMode.valueOf(intent.getStringExtra(EXTRA_MODE) ?: InterpolationMode.LINEAR.name)
        val speed = intent.getFloatExtra(EXTRA_SPEED, 1f)
        val usePregenerated = intent.getBooleanExtra(EXTRA_USE_PREGENERATED, false)
        bookmark = intent.getSerializableExtra(EXTRA_BOOKMARK) as? Bookmark

        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        playJob = playBook(
            scope = scope,
            session = TtsManager.session,
            phonemeConverter = TtsManager.phonemeConverter,
            styleLoader = TtsManager.styleLoader,
            selectedStyles = selectedStyles,
            weights = weights,
            mode = mode,
            speed = speed,
            lines = lines,
            startLine = currentLine,
            bookUri = bookUri?.let { Uri.parse(it) },
            audioPlayer = audioPlayer,
            context = this,
            onLineChanged = { line -> currentLine = line; updateNotification() },
            onFinished = {
                bookUri?.let { BookmarkManager.clear(this, it) }
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            },
            bookmark = bookmark,
            usePregenerated = usePregenerated
        )
    }

    private fun stopPlayback() {
        playJob?.cancel()
        playJob = null
        audioPlayer.stop()
    }

    private fun updateNotification() {
        val notification = buildNotification()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(): Notification {
        val pauseAction = if (audioPlayer.getState() == PlayerState.PLAYING) ACTION_PAUSE else ACTION_RESUME
        val pauseIntent = Intent(this, BookPlaybackService::class.java).apply { action = pauseAction }
        val pausePending = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, BookPlaybackService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(if (currentLine >= 0) "Line ${currentLine + 1}" else "")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(audioPlayer.getState() == PlayerState.PLAYING)
            .addAction(
                R.mipmap.ic_launcher,
                if (audioPlayer.getState() == PlayerState.PLAYING) "Pause" else "Resume",
                pausePending
            )
            .addAction(R.mipmap.ic_launcher, "Stop", stopPending)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Book Playback", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "book_playback"
        const val NOTIFICATION_ID = 1001
        const val ACTION_PAUSE = "com.example.kokoro82m.PAUSE"
        const val ACTION_RESUME = "com.example.kokoro82m.RESUME"
        const val ACTION_STOP = "com.example.kokoro82m.STOP"
        const val EXTRA_LINES = "lines"
        const val EXTRA_START_LINE = "start_line"
        const val EXTRA_BOOK_URI = "book_uri"
        const val EXTRA_SELECTED_STYLES = "styles"
        const val EXTRA_WEIGHTS = "weights"
        const val EXTRA_MODE = "mode"
        const val EXTRA_SPEED = "speed"
        const val EXTRA_BOOKMARK = "bookmark"
        const val EXTRA_USE_PREGENERATED = "use_pregenerated"
    }
}
