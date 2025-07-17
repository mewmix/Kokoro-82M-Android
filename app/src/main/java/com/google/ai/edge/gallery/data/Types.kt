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

enum class Screen(val route: String, val title: String, val shouldShowBottomBar: Boolean = true) {
  Home("home", "Home", true),
  ModelManager("model_manager", "Model Manager", true),
  LlmChat("llm_chat", "AI Chat", false),
  LlmSingleTurn("llm_single_turn", "Prompt Lab", false),
  ImageGen("image_gen", "Image Gen", false),
  ImageClassification("image_classification", "Image Classification", false),
  ObjectDetection("object_detection", "Object Detection", false),
  AudioClassification("audio_classification",                      "Audio Classification",
                      false),
  TextSummarization("text_summarization", "Text Summarization", false),
  TextEmbedding("text_embedding", "Text Embedding", false),
}

sealed class NavItem(val screen: Screen, val icon: Int, val label: String) {
  object Home : NavItem(Screen.Home, R.drawable.logo, "Home")

  object ModelManager : NavItem(Screen.ModelManager, R.drawable.deploy_code, "Model Manager")
}
