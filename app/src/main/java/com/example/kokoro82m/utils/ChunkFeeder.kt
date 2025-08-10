package com.example.kokoro82m.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object ChunkFeeder {
    private var job: Job? = null

    fun start(scope: CoroutineScope, chunkFlow: kotlinx.coroutines.flow.Flow<String>) {
        stop()
        job = scope.launch {
            chunkFlow.collectLatest { chunk ->
                val bounded = chunk.take(PhonemizerLimits.MAX_CHARS_PER_UTTERANCE)
                enqueueText(bounded)
                awaitChunkFinished()
            }
        }
    }

    fun stop() { job?.cancel(); job = null }

    // Map these to your existing TTS/queue
    private suspend fun enqueueText(text: String) {
        // TODO: Player.enqueue(text) or TtsEngine.speak(text)
    }
    private suspend fun awaitChunkFinished() {
        // TODO: Player.awaitIdle() or TtsEngine.awaitIdle()
    }
}
