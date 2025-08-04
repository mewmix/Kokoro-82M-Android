package com.example.kokoro82m.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.onnxruntime.OrtSession
import com.example.kokoro82m.utils.AudioPlayer
import com.example.kokoro82m.utils.InterpolationMode
import com.example.kokoro82m.utils.PhonemeConverter
import com.example.kokoro82m.utils.PlayerState
import com.example.kokoro82m.utils.StyleLoader
import com.example.kokoro82m.utils.playBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.readium.adapter.pdfium.document.PdfiumDocumentFactory
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.services.content.Content
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.toAbsoluteUrl
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser

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

    private var playJob: Job? = null

    @OptIn(ExperimentalReadiumApi::class)
    fun loadBook(context: Context, uri: Uri) {
        _bookUri.value = uri
        viewModelScope.launch(Dispatchers.IO) {
            val httpClient = DefaultHttpClient()
            val assetRetriever = AssetRetriever(context.contentResolver, httpClient)
            val asset = uri.toAbsoluteUrl()?.let { assetRetriever.retrieve(it).getOrNull() }

            val text = asset?.let {
                val publication = PublicationOpener(
                    DefaultPublicationParser(
                        context = context,
                        httpClient = httpClient,
                        assetRetriever = assetRetriever,
                        pdfFactory = PdfiumDocumentFactory(context),
                    ),
                ).open(it, allowUserInteraction = false).getOrNull()

                val content = publication?.content()
                buildString {
                    content?.forEach { element ->
                        val line = (element as? Content.TextualElement)?.text
                        if (!line.isNullOrBlank()) {
                            append(line).append('\n')
                        }
                    }
                }
            }
                ?: try {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                } catch (e: Exception) {
                    null
                } ?: ""

            withContext(Dispatchers.Main) {
                _lines.value = text.lines()
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
    }
}
