package com.example.kokoro.galleryport

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

class LlmController private constructor(val llm: LlmInference) {

    companion object {
        fun bootstrap(ctx: Context, modelId: String): LlmController? {
            val modelManager = com.example.kokoro82m.data.ModelManager(ctx)
            val model = modelManager.getModel(modelId)

            if (model == null || !model.isDownloaded) {
                return null
            }

            val modelFile = File(ctx.filesDir, "models/${model.id}.task")
            if (!modelFile.exists()) return null


            val opts = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(4096)
                .build()
            return LlmController(LlmInference.createFromOptions(ctx, opts))
        }
    }

    fun stream(prompt: String): Flow<String> = callbackFlow {
        llm.generateResponseAsync(prompt) { partialResult, done ->
            partialResult?.let(::trySend)
            if (done) {
                close()
            }
        }
        awaitClose { }
    }
}
