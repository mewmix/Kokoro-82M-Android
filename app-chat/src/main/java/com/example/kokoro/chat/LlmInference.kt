package com.example.kokoro.chat

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import java.io.File

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileOutputStream

data class LlmParameters(
    val topK: Int = 1,
    val temperature: Float = 0.8f,
    val randomSeed: Int = 101
)

class LlmInference(
    private val context: Context,
    private val modelPath: String
) {

    private var llmInference: LlmInference? = null
    private var currentParams: LlmParameters = LlmParameters()

    fun initialize(params: LlmParameters = currentParams) {
        llmInference?.close()
        currentParams = params
        val options = LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setTopK(params.topK)
            .setTemperature(params.temperature)
            .setRandomSeed(params.randomSeed)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
    }

    fun updateParameters(params: LlmParameters) {
        initialize(params)
    }

    fun sendMessage(prompt: String, resultListener: (partialResult: String, done: Boolean) -> Unit) {
        if (llmInference == null) {
            initialize()
        }

        llmInference?.generateResponseAsync(prompt, resultListener)
    }

    fun close() {
        llmInference?.close()
    }

}
