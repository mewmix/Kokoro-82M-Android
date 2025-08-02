package com.example.kokoro82m.utils

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder

class StreamingAudioPlayer(
    private val scope: CoroutineScope,
    private val onStateChanged: (PlayerState) -> Unit
) {
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val audioChannel = Channel<FloatArray>(Channel.UNLIMITED)

    private val sampleRate = 22050

    fun start() {
        if (playbackJob?.isActive == true) return
        playbackJob = scope.launch(Dispatchers.IO) {
            initAudioTrack()
            audioTrack?.play()
            onStateChanged(PlayerState.PLAYING)

            try {
                // This loop consumes audio chunks from the channel and writes them to the AudioTrack.
                for (audioChunk in audioChannel) {
                    if (!isActive) break
                    writeAudioData(audioChunk)
                }
            } finally {
                stopAndRelease()
            }
        }
    }

    // Called by the TTS generator to add audio to the playback queue.
    fun queueAudio(audioData: FloatArray) {
        if (!audioChannel.isClosedForSend) {
            scope.launch {
                audioChannel.send(audioData)
            }
        }
    }

    // Call this when the LLM response is complete to signal the end of the audio stream.
    fun stop() {
        audioChannel.close() // Closing the channel gracefully terminates the consumer loop.
    }

    private fun initAudioTrack() {
        if (audioTrack != null) return
        val bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        audioTrack = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build(),
            bufferSize * 2, // Use a larger buffer for streaming to avoid underruns.
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
    }

    private fun writeAudioData(audioData: FloatArray) {
        val pcmData = convertFloatToPcm16(audioData)
        audioTrack?.write(pcmData, 0, pcmData.size)
    }

    private fun convertFloatToPcm16(audioData: FloatArray): ByteArray {
        val byteBuffer = ByteBuffer.allocate(audioData.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        val shortBuffer = byteBuffer.asShortBuffer()
        for (sample in audioData) {
            val pcmValue = (sample * Short.MAX_VALUE).coerceIn(Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat()).toInt().toShort()
            shortBuffer.put(pcmValue)
        }
        return byteBuffer.array()
    }

    private fun stopAndRelease() {
        audioTrack?.let {
            if (it.playState == AudioTrack.PLAYSTATE_PLAYING) it.stop()
            it.flush()
            it.release()
        }
        audioTrack = null
        onStateChanged(PlayerState.IDLE)
    }
}

