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

package com.google.ai.edge.gallery.ui.llmsingleturn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.edge.gallery.ui.llmchat.LlmChatModelHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LlmSingleTurnViewModel
@Inject
constructor(private val llmChatModelHelper: LlmChatModelHelper) : ViewModel() {
  private val _response = MutableStateFlow("")
  val response = _response.asStateFlow()

  fun onPromptSelected(prompt: String) {
    viewModelScope.launch {
      val llmInference = llmChatModelHelper.getLlmInference()
      _response.value = llmInference.generateResponse(prompt)
    }
  }

  override fun onCleared() {
    super.onCleared()
    llmChatModelHelper.close()
  }
}
