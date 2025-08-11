package com.example.kokoro82m.utils

import kotlinx.coroutines.Job

object AudioPlayerManager {
    var player: AudioPlayer? = null
    var playJob: Job? = null

    fun stop() {
        playJob?.cancel()
        playJob = null
        player?.stop()
        player = null
    }
}
