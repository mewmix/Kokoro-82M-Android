package com.example.kokoro.chat

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import java.io.File

class LlmInference(
    private val context: Context,
    private val modelPath: String
) {

    private var llmInference: LlmInference? = null

    fun initialize() {
        val options = LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
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

    companion object {
        fun getModelFile(context: Context, modelName: String): File {
            val modelFile = File(context.cacheDir, modelName)
            if (!modelFile.exists()) {
                // In a real app, you would download the model here.
                // For this example, we'll just create an empty file.
                modelFile.createNewFile()
            }
            return modelFile
        }
    }
}
