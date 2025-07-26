package com.example.kokoro.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(val message: String, val isFromUser: Boolean)

class ChatViewModel(private val llmInference: LlmInference) : ViewModel() {

    private val _chatState = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatState: StateFlow<List<ChatMessage>> = _chatState.asStateFlow()

    fun sendMessage(message: String) {
        _chatState.value = _chatState.value + ChatMessage(message, true)

        viewModelScope.launch {
            val fullResponse = StringBuilder()
            llmInference.sendMessage(message) { partialResult, done ->
                fullResponse.append(partialResult)
                if (done) {
                    _chatState.value = _chatState.value + ChatMessage(fullResponse.toString(), false)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        llmInference.close()
    }
}
