package com.example.kokoro82m.utils

import ai.onnxruntime.OrtSession
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend fun playAudio(
    line: String,
    styleVector: Array<FloatArray>,
    speed: Float,
    session: OrtSession,
    phonemeConverter: PhonemeConverter,
    audioPlayer: AudioPlayer,
    position: Int = 0,
) {
    try {
        DebugLogger.log("Playing audio for line: '$line' from position $position")
        val phonemes = phonemeConverter.phonemize(line)
        val (audio, _) = createAudioFromStyleVector(
            phonemes = phonemes,
            voice = styleVector,
            speed = speed,
            session = session,
        )
        audioPlayer.prepare(audio, position)
        audioPlayer.playBlocking()
    } catch (e: Exception) {
        DebugLogger.log("playAudio failed: ${e.localizedMessage}")
    }
}

fun playBook(
    scope: CoroutineScope,
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
    audioPlayer: AudioPlayer,
    context: Context,
    onLineChanged: (Int) -> Unit,
    onFinished: () -> Unit,
    bookmark: Bookmark?,
): Job {
    return scope.launch(Dispatchers.IO) {
        DebugLogger.log("Starting playbook from line $startLine")
        var completed = true
        try {
            val mixedVector = mixStyles(
                styleLoader = styleLoader,
                styles = selectedStyles,
                weights = weights,
                mode = mode,
            )
            for (index in startLine until lines.size) {
                if (!isActive) {
                    completed = false
                    break
                }

                withContext(Dispatchers.Main) {
                    onLineChanged(index)
                }
                val line = lines[index]
                val position = if (index == bookmark?.line) bookmark.position else 0
                DebugLogger.log("Playing line $index with position $position")

                playAudio(
                    line = line,
                    styleVector = mixedVector,
                    speed = speed,
                    session = session,
                    phonemeConverter = phonemeConverter,
                    audioPlayer = audioPlayer,
                    position = position,
                )

                if (audioPlayer.getState() == PlayerState.PAUSED) {
                    completed = false
                    break
                }
            }
        } catch (e: Exception) {
            completed = false
            DebugLogger.log("playBook failed: ${e.localizedMessage}")
        } finally {
            withContext(Dispatchers.Main) {
                if (audioPlayer.getState() != PlayerState.PAUSED) {
                    onLineChanged(-1)
                    if (completed) {
                        onFinished()
                    }
                    audioPlayer.stop()
                }
            }
        }
    }
}

