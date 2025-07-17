/*
 * Copyright 2025 Google LLC
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

package com.google.ai.edge.gallery.ui.llmchat

import android.content.Context
import com.google.ai.edge.gallery.data.Model
import com.google.mediapipe.tasks.genai.llminference.LlmInference

class LlmChatModelHelper(private val context: Context, private val model: Model) {
  private var llmInference: LlmInference? = null

  fun getLlmInference(): LlmInference {
    if (llmInference == null) {
      val options = LlmInference.LlmInferenceOptions.builder()
        .setModelPath(model.getLocalModelUri(context).path)
        .build()
      llmInference = LlmInference.createFromOptions(context, options)
    }
    return llmInference!!
  }

  fun close() {
    llmInference?.close()
  }
}
