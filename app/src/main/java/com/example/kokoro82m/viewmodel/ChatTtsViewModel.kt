package com.example.kokoro82m.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.onnxruntime.OrtSession
import com.example.kokoro.chat.ChatMessage
import com.example.kokoro.chat.LlmInference
import com.example.kokoro82m.utils.InterpolationMode
import com.example.kokoro82m.utils.PhonemeConverter
import com.example.kokoro82m.utils.PlayerState
import com.example.kokoro82m.utils.SentenceSplitter
import com.example.kokoro82m.utils.StyleLoader
import com.example.kokoro82m.utils.StreamingAudioPlayer
import com.example.kokoro82m.utils.createAudioFromStyleVector
import com.example.kokoro82m.utils.mixStyles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.StringBuilder

class ChatTtsViewModel(
    private val context: Context,
    private val ortSession: OrtSession,
    private val llmInference: LlmInference
) : ViewModel() {

    // Dependencies
    private val phonemeConverter = PhonemeConverter(context)
    val styleLoader = StyleLoader(context)

    // Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // TTS & Player State
    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    val playerState = _playerState.asStateFlow()

    private val streamingAudioPlayer = StreamingAudioPlayer(viewModelScope) { newState ->
        _playerState.value = newState
    }

    // Mixer State
    private val _selectedStyles = MutableStateFlow(listOf("af_sarah"))
    val selectedStyles = _selectedStyles.asStateFlow()
    private val _weights = MutableStateFlow(mapOf("af_sarah" to 1f))
    val weights = _weights.asStateFlow()
    private val _interpolationMode = MutableStateFlow(InterpolationMode.LINEAR)
    val interpolationMode = _interpolationMode.asStateFlow()
    private val _speed = MutableStateFlow(1.0f)
    val speed = _speed.asStateFlow()

    init {
        llmInference.initialize()
    }

    fun sendMessage(message: String) {
        // Prevent new messages while one is processing
        if (_isLoading.value || _playerState.value == PlayerState.PLAYING) return
        
        _chatMessages.value += ChatMessage(message, true)
        _isLoading.value = true
        _chatMessages.value += ChatMessage("...", false) // UI placeholder

        // --- Start of new streaming logic ---
        streamingAudioPlayer.start() // Prepare the player to receive audio data.

        val responseBuilder = StringBuilder()
        
        // This helper will feed us complete sentences.
        val sentenceSplitter = SentenceSplitter { sentence ->
            // Launch a new background job for each sentence to generate TTS concurrently.
            viewModelScope.launch {
                synthesizeAndQueue(sentence)
            }
        }

        llmInference.sendMessage(message) { partialResult, done ->
            viewModelScope.launch(Dispatchers.Main) { // UI updates must happen on the Main thread.
                responseBuilder.append(partialResult)
                
                // Update the UI with the latest streaming text from the LLM.
                val last = _chatMessages.value.last()
                _chatMessages.value = _chatMessages.value.dropLast(1) + last.copy(message = responseBuilder.toString())

                if (!done) {
                    sentenceSplitter.process(partialResult)
                } else {
                    _isLoading.value = false
                    sentenceSplitter.flush() // Process any remaining text in the buffer.
                    
                    // Signal the player that no more audio is coming. It will finish playing its queue and then stop.
                    streamingAudioPlayer.stop()
                }
            }
        }
    }

    private suspend fun synthesizeAndQueue(text: String) {
        if (text.isBlank()) return
        
        try {
            // Perform all heavy computation on a background thread.
            val (audioData, _) = withContext(Dispatchers.IO) {
                val mixedVector = mixStyles(styleLoader, _selectedStyles.value, _weights.value, _interpolationMode.value)
                val phonemes = phonemeConverter.phonemize(text)
                createAudioFromStyleVector(phonemes, mixedVector, _speed.value, ortSession)
            }
            // Queue the generated audio for playback.
            streamingAudioPlayer.queueAudio(audioData)

        } catch (e: Exception) {
            Log.e("ChatTtsViewModel", "Error synthesizing audio for text: '$text'", e)
        }
    }

    // --- Mixer State Updaters ---
    fun addStyle(style: String) {
        if (style !in _selectedStyles.value) {
            _selectedStyles.value += style
            _weights.value += (style to 1f)
        }
    }

    fun removeStyle(style: String) {
        _selectedStyles.value -= style
        _weights.value -= style
        if (_selectedStyles.value.isEmpty()) {
            addStyle("af_sarah") // Ensure at least one style is always selected.
        }
    }

    fun updateWeight(style: String, value: Float) {
        _weights.value = _weights.value.toMutableMap().apply { this[style] = value }
    }

    fun updateInterpolationMode(mode: InterpolationMode) {
        _interpolationMode.value = mode
    }

    fun updateSpeed(newSpeed: Float) {
        _speed.value = newSpeed
    }

    override fun onCleared() {
        super.onCleared()
        llmInference.close()
        streamingAudioPlayer.stop() // Ensure all resources are released.
    }
}

