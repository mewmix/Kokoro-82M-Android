package com.example.kokoro82m.edgechat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(app: Application) : AndroidViewModel(app) {
    private val llmManager = LlmInferenceManager(app)

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages.asStateFlow()

    private val _actionFlow = MutableSharedFlow<String>()
    val actionFlow: SharedFlow<String> = _actionFlow.asSharedFlow()

    fun sendUserMessage(text: String) {
        _messages.update { it + "You: $text" + "\n" + "Bot: " }
        val index = _messages.value.lastIndex
        viewModelScope.launch {
            llmManager.generate(text).collect { tok ->
                _messages.update { current ->
                    val list = current.toMutableList()
                    val botMsg = list[index]
                    list[index] = botMsg + tok
                    list.toList()
                }
                _actionFlow.emit(tok)
            }
        }
    }
}

