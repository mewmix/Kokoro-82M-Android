package com.example.kokoro82m.viewmodel

import android.content.Context
import android.net.Uri
import android.text.Html
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.onnxruntime.OrtSession
import com.example.kokoro82m.utils.AudioPlayer
import com.example.kokoro82m.utils.AudioPlayerManager
import com.example.kokoro82m.utils.InterpolationMode
import com.example.kokoro82m.utils.PhonemeConverter
import com.example.kokoro82m.utils.PlayerState
import com.example.kokoro82m.utils.PlaybackNotification
import com.example.kokoro82m.utils.StyleLoader
import com.example.kokoro82m.utils.playBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.zip.ZipInputStream

class BookViewModel : ViewModel() {
    private val _bookUri = MutableStateFlow<Uri?>(null)
    val bookUri = _bookUri.asStateFlow()

    private val _lines = MutableStateFlow<List<String>>(emptyList())
    val lines = _lines.asStateFlow()

    private val _currentLine = MutableStateFlow(-1)
    val currentLine = _currentLine.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    val playerState = _playerState.asStateFlow()

    private var appContext: Context? = null

    val audioPlayer = AudioPlayer(
        scope = viewModelScope,
        onStateChanged = { state ->
            _playerState.value = state
            appContext?.let { PlaybackNotification.update(it, state) }
        }
    )

    private var playJob: Job? = null

    fun loadBook(context: Context, uri: Uri) {
        _bookUri.value = uri
        viewModelScope.launch(Dispatchers.IO) {
            val lines = try {
                val type = context.contentResolver.getType(uri) ?: ""
                val isEpub = type == "application/epub+zip" ||
                    uri.toString().endsWith(".epub", ignoreCase = true)
                if (isEpub) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        val builder = StringBuilder()
                        val zip = ZipInputStream(input)
                        var entry = zip.nextEntry
                        while (entry != null) {
                            if (!entry.isDirectory && (entry.name.endsWith(".xhtml", true) || entry.name.endsWith(".html", true))) {
                                val text = zip.readBytes().toString(Charsets.UTF_8)
                                val cleaned = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
                                builder.append(cleaned).append('\n')
                            }
                            zip.closeEntry()
                            entry = zip.nextEntry
                        }
                        builder.toString().lines()
                    } ?: emptyList()
                } else {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText().lines() }
                        ?: emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
            withContext(Dispatchers.Main) {
                _lines.value = lines
            }
        }
    }

    fun setCurrentLine(line: Int) {
        _currentLine.value = line
    }

    fun startPlayback(
        session: OrtSession,
        phonemeConverter: PhonemeConverter,
        styleLoader: StyleLoader,
        selectedStyles: List<String>,
        weights: Map<String, Float>,
        mode: InterpolationMode,
        speed: Float,
        lines: List<String>,
        startLine: Int,
        bookUri: Uri?,
        context: Context,
        usePregenerated: Boolean,
        onFinished: () -> Unit,
    ) {
        playJob?.cancel()
        appContext = context.applicationContext
        AudioPlayerManager.player = audioPlayer
        PlaybackNotification.show(appContext!!, true)
        playJob = playBook(
            scope = viewModelScope,
            session = session,
            phonemeConverter = phonemeConverter,
            styleLoader = styleLoader,
            selectedStyles = selectedStyles,
            weights = weights,
            mode = mode,
            speed = speed,
            lines = lines,
            startLine = startLine,
            bookUri = bookUri,
            audioPlayer = audioPlayer,
            context = context,
            onLineChanged = { setCurrentLine(it) },
            onFinished = onFinished,
            usePregenerated = usePregenerated,
        )
    }

    fun stopPlayback() {
        playJob?.cancel()
        playJob = null
        audioPlayer.stop()
        appContext?.let { PlaybackNotification.cancel(it) }
        AudioPlayerManager.player = null
    }
}
