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

package com.google.ai.edge.gallery.ui.common.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.edge.gallery.data.Message
import com.google.ai.edge.gallery.data.Sender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
  private val _messages = MutableStateFlow<List<Message>>(emptyList())
  val messages = _messages.asStateFlow()

  private val _isRecording = MutableStateFlow(false)
  val isRecording = _isRecording.asStateFlow()

  fun sendMessage(text: String) {
    viewModelScope.launch {
      val userMessage = Message(text, Sender.USER)
      _messages.value = _messages.value + userMessage

      // TODO: Add model logic here
      val modelMessage = Message("Model response", Sender.MODEL)
      _messages.value = _messages.value + modelMessage
    }
  }

  fun startRecording() {
    _isRecording.value = true
  }

  fun stopRecording() {
    _isRecording.value = false
  }
}
