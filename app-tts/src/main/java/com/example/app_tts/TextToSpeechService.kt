package com.example.app_tts

import ai.onnxruntime.OrtSession
import android.content.Context
import com.example.kokoro82m.utils.PhonemeConverter
import com.example.kokoro82m.utils.createAudio
import com.example.kokoro82m.utils.playAudio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TextToSpeechService(
    private val context: Context,
    private val getKokoroSession: () -> OrtSession,
    private val scope: CoroutineScope
) {
    private val phonemeConverter = PhonemeConverter(context)

    fun speakWithKokoro(text: String, voice: String, speed: Float) {
        scope.launch(Dispatchers.IO) {
            val phonemes = phonemeConverter.phonemize(text)
            val (audioData, _) = createAudio(
                phonemes = phonemes,
                voice = voice,
                speed = speed,
                session = getKokoroSession(),
                context = context
            )
            playAudio(audioData, scope) {}
        }
    }
}
