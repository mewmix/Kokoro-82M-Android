package com.example.kokoro82m.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PlaybackReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val player = AudioPlayerManager.player ?: return
        when (intent.action) {
            ACTION_PLAY -> {
                player.play()
                PlaybackNotification.update(context, PlayerState.PLAYING)
            }
            ACTION_PAUSE -> {
                player.pause()
                PlaybackNotification.update(context, PlayerState.PAUSED)
            }
            ACTION_STOP -> {
                player.stop()
                PlaybackNotification.update(context, PlayerState.IDLE)
            }
        }
    }

    companion object {
        const val ACTION_PLAY = "com.example.kokoro82m.PLAYBACK_PLAY"
        const val ACTION_PAUSE = "com.example.kokoro82m.PLAYBACK_PAUSE"
        const val ACTION_STOP = "com.example.kokoro82m.PLAYBACK_STOP"
    }
}
