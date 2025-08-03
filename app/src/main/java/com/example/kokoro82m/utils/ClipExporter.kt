package com.example.kokoro82m.utils

import ai.onnxruntime.OrtSession
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Generates audio for a selection of lines and exports it as a single WAV file.
 */
suspend fun exportClip(
    context: Context,
    session: OrtSession,
    phonemeConverter: PhonemeConverter,
    styleLoader: StyleLoader,
    selectedStyles: List<String>,
    weights: Map<String, Float>,
    mode: InterpolationMode,
    speed: Float,
    lines: List<String>,
    startLine: Int,
    endLine: Int,
    fileName: String,
    bookUri: Uri?,
    usePregenerated: Boolean
): Uri? = withContext(Dispatchers.IO) {
    val mixedVector = mixStyles(
        styleLoader = styleLoader,
        styles = selectedStyles,
        weights = weights,
        mode = mode
    )
    val audioData = mutableListOf<Float>()
    for (index in startLine..endLine) {
        if (usePregenerated && bookUri != null) {
            val path = DatabaseManager.getAudioLine(context, bookUri.toString(), index)
            if (path != null) {
                val audio = loadAudioInternal(File(path))
                audioData.addAll(audio.toList())
                continue
            }
        }
        val phonemes = phonemeConverter.phonemize(lines[index])
        val (audio, _) = createAudioFromStyleVector(
            phonemes = phonemes,
            voice = mixedVector,
            speed = speed,
            session = session
        )
        audioData.addAll(audio.toList())
    }
    saveAudio(audioData.toFloatArray(), context, fileName)
}
