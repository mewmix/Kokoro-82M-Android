package com.example.kokoro.galleryport

import android.content.Context
import android.graphics.Bitmap
import com.example.kokoro82m.utils.PerfProfiler
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.genai.llminference.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

object AskImagePipeline {
    suspend fun run(ctx: Context, bitmap: Bitmap, q: String): Flow<String> = callbackFlow {
        val ctrl = LlmController.bootstrap(ctx)
        if (ctrl == null) {
            close(IllegalStateException("LlmController could not be bootstrapped"))
            return@callbackFlow
        }
        ctrl.let { // Use let for safe operations on nullable LlmController
            val sess = PerfProfiler.track("Vision Session Create") {
                LlmInferenceSession.createFromOptions(
                    it.llm,
                    LlmInferenceSession.LlmInferenceSessionOptions.builder()
                        .setGraphOptions(GraphOptions.builder().setEnableVisionModality(true).build())
                        .build()
                )
            }
            sess.addImage(BitmapImageBuilder(bitmap).build())
            sess.addQueryChunk(q)
            sess.generateResponseAsync { res, done -> trySend(res); if (done) close() }
        }
        awaitClose { }
    }
}
