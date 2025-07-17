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

package com.google.ai.edge.gallery.data

import com.google.ai.edge.gallery.R

object Tasks {
  val items = listOf("llm_chat", "llm_ask_image", "llm_prompt_lab")

  fun getDisplayName(task: String): String {
    return when (task) {
      "llm_chat" -> "AI Chat"
      "llm_ask_image" -> "Ask Image"
      "llm_prompt_lab" -> "Prompt Lab"
      else -> ""
    }
  }

  fun getIcon(task: String): Int {
    return when (task) {
      "llm_chat" -> R.drawable.chat_spark
      "llm_ask_image" -> R.drawable.image_spark
      "llm_prompt_lab" -> R.drawable.text_spark
      else -> R.drawable.logo
    }
  }
}
