package com.example.kokoro.galleryport

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.nio.MappedByteBuffer

class LlmController private constructor(val llm: LlmInference) {

    companion object {
        suspend fun bootstrap(ctx: Context): LlmController {
            // This is the model we will be using. Feel free to change it to any other model.
            val modelUrl = "https://huggingface.co/google/gemma-2b-it-cpu/resolve/main/gemma-2b-it-cpu-int4.bin"
            val modelAlias = "gemma-2b-it-cpu-int4.bin"

            val f = ModelHub.get(
                ctx,
                modelUrl,
                modelAlias
            )
            val opts = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(f.absolutePath)
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
