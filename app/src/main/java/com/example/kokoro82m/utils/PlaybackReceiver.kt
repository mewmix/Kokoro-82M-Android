package com.example.kokoro82m.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PlaybackReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val player = AudioPlayerManager.player ?: return
        when (intent.action) {
            ACTION_TOGGLE -> {
                if (player.getState() == PlayerState.PLAYING) {
                    player.pause()
                    PlaybackNotification.update(context, PlayerState.PAUSED)
                } else {
                    player.play()
                    PlaybackNotification.update(context, PlayerState.PLAYING)
                }
            }
            ACTION_STOP -> {
                player.stop()
                PlaybackNotification.update(context, PlayerState.IDLE)
                AudioPlayerManager.player = null
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE = "com.example.kokoro82m.PLAYBACK_TOGGLE"
        const val ACTION_STOP = "com.example.kokoro82m.PLAYBACK_STOP"
    }
}
