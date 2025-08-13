package com.example.kokoro82m.llm

import android.content.Context
import com.example.kokoro82m.data.*
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import java.io.File

/**
 * Helper to create [LlmInferenceSession] instances with configurable generation
 * parameters. Defaults are read from the model allowlist but callers may
 * override them by providing an [InferenceOptions] instance.
 */
object LlmChatModelHelper {

    data class InferenceOptions(
        val maxTokens: Int? = null,
        val topK: Int? = null,
        val topP: Float? = null,
        val temperature: Float? = null,
    )

    fun initialize(
        context: Context,
        model: Model,
        options: InferenceOptions = InferenceOptions(),
    ): LlmInferenceSession {
        val maxTokens = options.maxTokens
            ?: model.getIntConfigValue(ConfigKeys.MAX_TOKENS, DEFAULT_MAX_TOKEN)
        val topK = options.topK
            ?: model.getIntConfigValue(ConfigKeys.TOPK, DEFAULT_TOPK)
        val topP = options.topP
            ?: model.getFloatConfigValue(ConfigKeys.TOPP, DEFAULT_TOPP)
        val temperature = options.temperature
            ?: model.getFloatConfigValue(ConfigKeys.TEMPERATURE, DEFAULT_TEMPERATURE)

        val inferenceOptions = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(File(context.filesDir, "models/${model.id}.task").absolutePath)
            .setMaxTokens(maxTokens)
            .setMaxTopK(topK)
            .build()

        val llm = LlmInference.createFromOptions(context, inferenceOptions)

        val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
            .setTopK(topK)
            .setTopP(topP)
            .setTemperature(temperature)
            .build()

        return LlmInferenceSession.createFromOptions(llm, sessionOptions)
    }
}
