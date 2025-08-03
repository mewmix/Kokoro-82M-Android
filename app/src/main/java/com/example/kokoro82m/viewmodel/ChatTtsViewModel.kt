package com.example.kokoro82m.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.onnxruntime.OrtSession
import com.example.kokoro.chat.ChatMessage
import com.example.kokoro.chat.LlmInference
import com.example.kokoro82m.utils.AudioPlayer
import com.example.kokoro82m.utils.InterpolationMode
import com.example.kokoro82m.utils.PhonemeConverter
import com.example.kokoro82m.utils.PlayerState
import com.example.kokoro82m.utils.StyleLoader
import com.example.kokoro82m.utils.DebugLogger
import com.example.kokoro82m.utils.createAudioFromStyleVector
import com.example.kokoro82m.utils.mixStyles
import com.example.kokoro.chat.PerfHud
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
    private val audioPlayer = AudioPlayer(viewModelScope) { newState ->
        _playerState.value = newState
    }

    // Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // TTS State
    private val _isSynthesizing = MutableStateFlow(false)
    val isSynthesizing = _isSynthesizing.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    val playerState = _playerState.asStateFlow()

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
        DebugLogger.log("ChatTtsViewModel sendMessage: $message")
        _chatMessages.value += ChatMessage(message, true)
        _isLoading.value = true

        val responseBuilder = StringBuilder()
        _chatMessages.value += ChatMessage("...", false)  // placeholder

        viewModelScope.launch(Dispatchers.IO) { // 1. Outer scope for IO work
            llmInference.sendMessage(message) { partial, done -> // 2. This is a REGULAR callback
                if (!done) {
                    responseBuilder.append(partial)
                    val last = _chatMessages.value.last()
                    _chatMessages.value =
                        _chatMessages.value.dropLast(1) + last.copy(message = responseBuilder.toString())
                } else {
                    // 3. Hop back to the main thread safely for UI updates & suspend function calls
                    viewModelScope.launch { // Defaults to Main dispatcher for viewModelScope
                        _isLoading.value = false
                        DebugLogger.log("ChatTtsViewModel response complete")
                        synthesizeAndPlay(responseBuilder.toString()) // synthesizeAndPlay can now be called
                    }
                }
            }
        }
    }


    private fun synthesizeAndPlay(text: String) {
        viewModelScope.launch {
            _isSynthesizing.value = true
            try {
                // Perform all heavy computations on a background thread
                val audioData = withContext(Dispatchers.IO) {
                    PerfHud.record("TTS") {
                        val mixedVector = mixStyles(
                            styleLoader,
                            _selectedStyles.value,
                            _weights.value,
                            _interpolationMode.value
                        )
                        val phonemes = phonemeConverter.phonemize(text)

                        val (data, _) = createAudioFromStyleVector(
                            phonemes = phonemes,
                            voice = mixedVector,
                            speed = _speed.value,
                            session = ortSession
                        )
                        data // Return the resulting audio data
                    }
                }

                // Switch back to the main thread to update UI and play audio
                _isSynthesizing.value = false
                audioPlayer.prepare(audioData)
                audioPlayer.play()

            } catch (e: Exception) {
                _isSynthesizing.value = false
                // Handle error, e.g., show a toast
                android.util.Log.e("ChatTtsViewModel", "Error synthesizing audio", e)
            }
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
            addStyle("af_sarah") // Ensure at least one style is always selected
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
        audioPlayer.stop()
    }
}