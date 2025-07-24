/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.kokoro82m.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import java.io.File

private const val TAG = "LlmInferenceManager"

typealias ResultListener = (partialResult: String, done: Boolean) -> Unit

object LlmInferenceManager {
    private var llmInference: LlmInference? = null
    private var session: LlmInferenceSession? = null

    fun initialize(context: Context, modelPath: String) {
        if (llmInference != null) {
            return
        }
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .build()
        llmInference = LlmInference.createFromOptions(context, options)
        session = llmInference?.createSession()
    }

    fun generateResponse(prompt: String, resultListener: ResultListener) {
        session?.generateResponseAsync(prompt, resultListener)
    }

    fun release() {
        session?.close()
        session = null
        llmInference?.close()
        llmInference = null
    }
}
