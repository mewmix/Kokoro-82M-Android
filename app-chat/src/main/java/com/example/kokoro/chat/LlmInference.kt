package com.example.kokoro.chat

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import java.io.File
import com.example.kokoro82m.utils.DebugLogger

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileOutputStream

class LlmInference(
    private val context: Context,
    private val modelPath: String
) {

    private var llmInference: LlmInference? = null

    fun initialize() {
        DebugLogger.log("LlmInference initialize with model $modelPath")
        val options = LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
    }

    fun sendMessage(prompt: String, resultListener: (partialResult: String, done: Boolean) -> Unit) {
        if (llmInference == null) {
            initialize()
        }

        DebugLogger.log("LlmInference sendMessage: $prompt")
        val start = System.nanoTime()
        llmInference?.generateResponseAsync(prompt) { partial, done ->
            if (done) {
                val ms = (System.nanoTime() - start) / 1e6f
                PerfHud.recordValue("LLM", ms)
            }
            resultListener(partial, done)
        }
    }

    fun close() {
        llmInference?.close()
    }

}
