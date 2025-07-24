package com.example.kokoro82m.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kokoro82m.utils.LlmInferenceManager
import com.example.kokoro82m.utils.OnnxRuntimeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val context: Context) : ViewModel() {
    private val _chatMessages = MutableStateFlow(listOf<String>())
    val chatMessages: StateFlow<List<String>> = _chatMessages

    private val _isLlmInitialized = MutableStateFlow(false)
    val isLlmInitialized: StateFlow<Boolean> = _isLlmInitialized

    init {
        viewModelScope.launch {
            OnnxRuntimeManager.initialize(context.applicationContext)
        }
    }

    fun getSession() = OnnxRuntimeManager.getSession()

    fun initializeLlm(modelPath: String) {
        viewModelScope.launch {
            LlmInferenceManager.initialize(context, modelPath)
            _isLlmInitialized.value = true
        }
    }

    fun sendChatMessage(prompt: String) {
        viewModelScope.launch {
            _chatMessages.value += "> $prompt"
            var currentResponse = ""
            LlmInferenceManager.generateResponse(prompt) { partialResult, done ->
                currentResponse += partialResult
                _chatMessages.value = _chatMessages.value.dropLast(1) + ("> $prompt\n$currentResponse")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        LlmInferenceManager.release()
    }
}
