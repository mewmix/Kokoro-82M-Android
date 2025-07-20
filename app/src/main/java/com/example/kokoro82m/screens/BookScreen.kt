package com.example.kokoro82m.screens

import ai.onnxruntime.OrtSession
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kokoro82m.utils.*
import com.example.kokoro82m.viewmodel.BookViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    session: OrtSession,
    phonemeConverter: PhonemeConverter,
    bookViewModel: BookViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val lines by bookViewModel.lines.collectAsState()
    val currentLine by bookViewModel.currentLine.collectAsState()
    val playerState by bookViewModel.playerState.collectAsState()

    val bookUri by bookViewModel.bookUri.collectAsState()
    var bookmark by remember { mutableStateOf<Bookmark?>(null) }


    val listState = rememberLazyListState()

    val styleLoader = remember { StyleLoader(context) }
    var selectedStyles by remember { mutableStateOf(listOf("af_sarah")) }
    var weights by remember { mutableStateOf(mapOf("af_sarah" to 1f)) }
    var interpolationMode by remember { mutableStateOf(InterpolationMode.LINEAR) }
    var speed by remember { mutableFloatStateOf(SettingsManager.getSpeed(context)) }
    var debugMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            bookViewModel.loadBook(context, it)
        }
    }

    LaunchedEffect(bookUri) {
        bookUri?.let {
            bookmark = BookmarkManager.load(context, it.toString())
            DebugLogger.log("Loaded bookmark: $bookmark")
            bookmark?.let {
                bookViewModel.setCurrentLine(it.line)
            } ?: run {
                bookViewModel.setCurrentLine(0)
            }
        }
    }

    LaunchedEffect(currentLine) {
        if (currentLine >= 0) {
            listState.animateScrollToItem(currentLine)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = listState
    ) {
        item {
            StyleSelector(
                styleNames = styleLoader.names,
                selectedStyles = selectedStyles,
                onAddStyle = { style ->
                    selectedStyles = selectedStyles + style
                    weights = weights + (style to 1f)
                },
                onRemoveStyle = { style ->
                    selectedStyles = selectedStyles - style
                    weights = weights - style
                }
            )
        }

        item {
            WeightSliders(
                selectedStyles = selectedStyles,
                weights = weights,
                onWeightChanged = { style, value ->
                    weights = weights.toMutableMap().apply { put(style, value) }
                }
            )
        }

        item {
            InterpolationModeSelector(
                currentMode = interpolationMode,
                onModeSelected = { interpolationMode = it }
            )
        }

        item {
            Text("Speed: $speed")
        }

        item {
            Slider(
                value = speed,
                onValueChange = {
                    speed = it
                    SettingsManager.setSpeed(context, it)
                },
                valueRange = 0.5f..2.0f,
                steps = 5,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (bookmark != null && playerState == PlayerState.IDLE) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Resume at line ${bookmark!!.line + 1}")
                    Button(onClick = { bookViewModel.setCurrentLine(bookmark!!.line) }) {
                        Text("Go")
                    }
                    Button(onClick = {
                        bookUri?.let { BookmarkManager.clear(context, it.toString()) }
                        bookmark = null
                        bookViewModel.setCurrentLine(-1)
                    }) {
                        Text("Clear")
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(onClick = { launcher.launch(arrayOf("text/plain")) }, modifier = Modifier.weight(1f)) {
                    Text("Open")
                }
                Button(
                    onClick = {
                        if (playerState == PlayerState.PLAYING) {
                            bookViewModel.audioPlayer.pause()
                            bookUri?.let {
                                val position = bookViewModel.audioPlayer.getPosition()
                                DebugLogger.log("Saving bookmark at line $currentLine, position $position")
                                BookmarkManager.save(context, it.toString(), currentLine, position)
                            }
                        } else {
                            bookViewModel.startPlayback(
                                session = session,
                                phonemeConverter = phonemeConverter,
                                styleLoader = styleLoader,
                                selectedStyles = selectedStyles,
                                weights = weights,
                                mode = interpolationMode,
                                speed = speed,
                                lines = lines,
                                startLine = currentLine.coerceAtLeast(0),
                                bookUri = bookUri,
                                context = context,
                                bookmark = bookmark,
                                onFinished = {
                                    bookUri?.let { u -> BookmarkManager.clear(context, u.toString()) }
                                    bookmark = null
                                },
                            )
                        }
                    },
                    enabled = lines.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        when (playerState) {
                            PlayerState.IDLE -> "Play"
                            PlayerState.PLAYING -> "Pause"
                            PlayerState.PAUSED -> "Resume"
                        }
                    )
                }
                Button(
                    onClick = {
                        bookViewModel.stopPlayback()
                    },
                    enabled = playerState != PlayerState.IDLE,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stop")
                }
                Button(
                    onClick = {
                        isProcessing = true
                        scope.launch(Dispatchers.IO) {
                            try {
                                debugMessage = null
                                val mixedVector = mixStyles(
                                    styleLoader = styleLoader,
                                    styles = selectedStyles,
                                    weights = weights,
                                    mode = interpolationMode
                                )
                                val audioData = mutableListOf<Float>()
                                for (line in lines) {
                                    val phonemes = phonemeConverter.phonemize(line)
                                    val (audio, _) = createAudioFromStyleVector(
                                        phonemes = phonemes,
                                        voice = mixedVector,
                                        speed = speed,
                                        session = session
                                    )
                                    audioData.addAll(audio.toList())
                                }
                                val fileName = buildStyleFileName(selectedStyles, weights, interpolationMode)
                                saveAudio(audioData.toFloatArray(), context, fileName)
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    debugMessage = e.localizedMessage
                                 }
                            } finally {
                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                }
                            }
                        }
                    },
                    enabled = lines.isNotEmpty() && !isProcessing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isProcessing) "Saving..." else "Save")
                }
            }
        }

        itemsIndexed(lines) { index, line ->
            Text(
                text = line,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        bookViewModel.setCurrentLine(index)
                        bookViewModel.startPlayback(
                            session = session,
                            phonemeConverter = phonemeConverter,
                            styleLoader = styleLoader,
                            selectedStyles = selectedStyles,
                            weights = weights,
                            mode = interpolationMode,
                            speed = speed,
                            lines = lines,
                            startLine = index,
                            bookUri = bookUri,
                            context = context,
                            bookmark = bookmark,
                            onFinished = {
                                bookUri?.let { u -> BookmarkManager.clear(context, u.toString()) }
                                bookmark = null
                            },
                        )
                    }
                    .background(if (index == currentLine) Color.Yellow else Color.Transparent)
                    .padding(4.dp)
            )
        }

        item {
            debugMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        item {
            if (SettingsManager.isDebug(context)) {
                val logs = DebugLogger.getLogs().joinToString("\n")
                Text(logs, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}


