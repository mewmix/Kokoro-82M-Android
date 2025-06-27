package com.example.kokoro82m.screens

import ai.onnxruntime.OrtSession
import androidx.compose.runtime.Composable
import com.example.kokoro82m.utils.PhonemeConverter
import com.example.kokoro82m.utils.StyleLoader

@Composable
fun MixerDemoScreen(
    session: OrtSession,
    phonemeConverter: PhonemeConverter,
    styleLoader: StyleLoader,
) {
    MixerScreen(
        session = session,
        phonemeConverter = phonemeConverter,
        styleLoader = styleLoader,
    )
}
