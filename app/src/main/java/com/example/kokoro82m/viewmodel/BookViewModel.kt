package com.example.kokoro82m.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kokoro82m.utils.AudioPlayer
import com.example.kokoro82m.utils.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookViewModel : ViewModel() {
    private val _bookUri = MutableStateFlow<Uri?>(null)
    val bookUri = _bookUri.asStateFlow()

    private val _lines = MutableStateFlow<List<String>>(emptyList())
    val lines = _lines.asStateFlow()

    private val _currentLine = MutableStateFlow(-1)
    val currentLine = _currentLine.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    val playerState = _playerState.asStateFlow()

    val audioPlayer = AudioPlayer(
        scope = viewModelScope,
        onStateChanged = { _playerState.value = it }
    )

    fun loadBook(context: Context, uri: Uri) {
        _bookUri.value = uri
        viewModelScope.launch(Dispatchers.IO) {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
            withContext(Dispatchers.Main) {
                _lines.value = text.lines()
            }
        }
    }

    fun setCurrentLine(line: Int) {
        _currentLine.value = line
    }
}
