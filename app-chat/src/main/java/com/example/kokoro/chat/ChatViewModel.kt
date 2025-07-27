package com.example.kokoro.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_tts.TextToSpeechService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(val message: String, val isFromUser: Boolean)

class ChatViewModel(
    private val llmInference: LlmInference,
    private val ttsService: TextToSpeechService
) : ViewModel() {

    private val _chatState = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatState: StateFlow<List<ChatMessage>> = _chatState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _params = MutableStateFlow(LlmParameters())
    val params: StateFlow<LlmParameters> = _params.asStateFlow()

    private val _isSpeakEnabled = MutableStateFlow(true)
    val isSpeakEnabled: StateFlow<Boolean> = _isSpeakEnabled.asStateFlow()

    fun updateParams(newParams: LlmParameters) {
        _params.value = newParams
        llmInference.updateParameters(newParams)
    }

    fun setSpeakEnabled(enabled: Boolean) {
        _isSpeakEnabled.value = enabled
    }

    fun sendMessage(message: String) {
        _chatState.value = _chatState.value + ChatMessage(message, true)
        _isLoading.value = true

        val responseBuilder = StringBuilder()

        viewModelScope.launch(Dispatchers.IO) {
            llmInference.sendMessage(message) { partialResult, done ->
                if (done) {
                    _isLoading.value = false
                    val finalText = responseBuilder.toString()
                    val lastMessage = _chatState.value.lastOrNull { !it.isFromUser }
                    if (lastMessage != null) {
                        val updated = lastMessage.copy(message = finalText)
                        _chatState.value = _chatState.value.dropLast(1) + updated
                    } else {
                        _chatState.value = _chatState.value + ChatMessage(finalText, false)
                    }
                    if (_isSpeakEnabled.value) {
                        ttsService.speakWithKokoro(finalText, "af_sarah", 1.0f)
                    }
                } else {
                    responseBuilder.append(partialResult)
                    val lastMessage = _chatState.value.lastOrNull { !it.isFromUser }
                    if (lastMessage != null) {
                        val updatedMessage = lastMessage.copy(message = responseBuilder.toString())
                        _chatState.value = _chatState.value.dropLast(1) + updatedMessage
                    } else {
                        _chatState.value = _chatState.value + ChatMessage(partialResult, false)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        llmInference.close()
    }
}
