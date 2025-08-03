package com.example.kokoro82m.utils

import ai.onnxruntime.OrtSession

/**
 * Holds global references to TTS resources so that background services can
 * access them without requiring serialization through intents.
 */
object TtsManager {
    lateinit var session: OrtSession
    lateinit var phonemeConverter: PhonemeConverter
    lateinit var styleLoader: StyleLoader
}
