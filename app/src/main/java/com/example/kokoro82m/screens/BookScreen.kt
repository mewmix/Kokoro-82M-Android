package com.example.kokoro82m.screens

import ai.onnxruntime.OrtSession
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kokoro82m.R
import com.example.kokoro82m.utils.*
import com.example.kokoro82m.viewmodel.BookViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    var showSettings by remember { mutableStateOf(true) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            bookViewModel.loadBook(context, it)
        }
    }

    LaunchedEffect(bookUri) {
        bookUri?.let { uri ->
            val project = ProjectManager.load(context, uri.toString())
            if (project != null) {
                selectedStyles = project.styles.ifEmpty { listOf("af_sarah") }
                weights = if (project.weights.isNotEmpty()) project.weights else mapOf("af_sarah" to 1f)
                interpolationMode = project.mode
                speed = project.speed
                bookmark = project.bookmark
                DebugLogger.log("Loaded project: $project")
                bookViewModel.setCurrentLine(project.bookmark?.line ?: 0)
            } else {
                bookmark = BookmarkManager.load(context, uri.toString())
                DebugLogger.log("Loaded bookmark: $bookmark")
                bookmark?.let { bookViewModel.setCurrentLine(it.line) } ?: bookViewModel.setCurrentLine(0)
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
            .padding(dimensionResource(id = R.dimen.padding_large)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
        state = listState
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSettings = !showSettings },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Settings", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Toggle Settings",
                            modifier = Modifier.graphicsLayer(rotationZ = if (showSettings) 180f else 0f)
                        )
                    }
                    if (showSettings) {
                        Divider(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_medium)))
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
                        WeightSliders(
                            selectedStyles = selectedStyles,
                            weights = weights,
                            onWeightChanged = { style, value ->
                                weights = weights.toMutableMap().apply { put(style, value) }
                            }
                        )
                        InterpolationModeSelector(
                            currentMode = interpolationMode,
                            onModeSelected = { interpolationMode = it }
                        )
                        Text("Speed: ${ "%.2f".format(speed)}", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = speed,
                            onValueChange = {
                                speed = it
                                SettingsManager.setSpeed(context, it)
                            },
                            valueRange = 0.5f..2.0f,
                            steps = 15,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (bookmark != null && playerState == PlayerState.IDLE) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
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
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small)),
                maxItemsInEachRow = 3
            ) {
                Button(onClick = { launcher.launch(arrayOf("text/plain")) }) {
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
                                val project = Project(
                                    uri = it.toString(),
                                    styles = selectedStyles,
                                    weights = weights,
                                    mode = interpolationMode,
                                    speed = speed,
                                    bookmark = Bookmark(currentLine, position)
                                )
                                ProjectManager.save(context, project)
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
                    enabled = lines.isNotEmpty()
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
                    enabled = playerState != PlayerState.IDLE
                ) {
                    Text("Stop")
                }
                Button(
                    onClick = {
                        isProcessing = true
                        scope.launch {
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
                                debugMessage = e.localizedMessage
                            } finally {
                                isProcessing = false
                            }
                        }
                    },
                    enabled = lines.isNotEmpty() && !isProcessing
                ) {
                    Text(if (isProcessing) "Saving..." else "Save")
                }
                Button(
                    onClick = {
                        bookUri?.let {
                            val project = Project(
                                uri = it.toString(),
                                styles = selectedStyles,
                                weights = weights,
                                mode = interpolationMode,
                                speed = speed,
                                bookmark = bookmark
                            )
                            ProjectManager.save(context, project)
                        }
                    }
                ) {
                    Text("Save Project")
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
                    .background(if (index == currentLine) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .padding(dimensionResource(id = R.dimen.padding_small)),
                textAlign = TextAlign.Justify,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        item {
            debugMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_medium))
                )
            }
        }

        item {
            if (SettingsManager.isDebug(context)) {
                val logs = DebugLogger.getLogs().joinToString("\n")
                Text(logs, modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_medium)))
            }
        }
    }
}


