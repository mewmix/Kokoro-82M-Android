package com.example.kokoro82m.edgechat

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import java.io.File

class LlmInferenceManager(private val context: Context) {
    companion object {
        private const val MODEL_ID = "TheModelOrg/my-edge-model-q4gg"
    }

    private var llm: LlmInference? = null

    /** Ensures that the model file exists and the LLM is created. */
    private fun ensureModel(): Boolean {
        if (llm != null) return true
        val modelFile = File(context.filesDir, "models/$MODEL_ID.task")
        if (!modelFile.exists()) return false
        val opts = LlmInferenceOptions.builder()
            .setModelPath(modelFile.absolutePath)
            .setMaxTokens(4096)
            .build()
        llm = LlmInference.createFromOptions(context, opts)
        return true
    }

    /**
     * Generates a response to [prompt] and emits partial tokens via [Flow].
     * This is a minimal placeholder implementation. Real apps should handle
     * model downloading and error states.
     */
    fun generate(prompt: String): Flow<String> = callbackFlow {
        if (!ensureModel()) {
            close()
            return@callbackFlow
        }
        llm?.generateResponseAsync(prompt) { part, done ->
            part?.let { trySend(it) }
            if (done) close()
        }
        awaitClose { }
    }
}

