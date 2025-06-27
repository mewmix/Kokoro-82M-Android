package com.example.kokoro82m.screens

import ai.onnxruntime.OrtSession
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.kokoro82m.utils.AudioPlayer
import com.example.kokoro82m.utils.PhonemeConverter
import com.example.kokoro82m.utils.createAudio
import com.example.kokoro82m.utils.saveAudio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    session: OrtSession,
    phonemeConverter: PhonemeConverter,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var lines by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentLine by remember { mutableIntStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }
    val audioPlayer = remember { AudioPlayer() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val text = readTextFromUri(context, it)
                lines = text.lines()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { launcher.launch(arrayOf("text/plain")) }) {
                Text("Open File")
            }
            Button(
                onClick = {
                    if (isPlaying) {
                        audioPlayer.pause()
                        isPlaying = false
                    } else {
                        audioPlayer.resume()
                        if (currentLine == -1 && lines.isNotEmpty()) {
                            playBook(
                                session,
                                phonemeConverter,
                                lines,
                                audioPlayer,
                                context,
                                scope,
                                onLineChanged = { currentLine = it },
                                onFinished = { isPlaying = false }
                            )
                        }
                        isPlaying = true
                    }
                },
                enabled = lines.isNotEmpty()
            ) {
                Text(if (isPlaying) "Pause" else "Play")
            }
            Button(
                onClick = {
                    scope.launch {
                        val audioData = mutableListOf<Float>()
                        for (line in lines) {
                            val phonemes = phonemeConverter.phonemize(line)
                            val (audio, _) = createAudio(
                                phonemes = phonemes,
                                voice = "af_sarah",
                                speed = 1.0f,
                                session = session,
                                context = context
                            )
                            audioData.addAll(audio.toList())
                        }
                        saveAudio(audioData.toFloatArray(), context)
                    }
                },
                enabled = lines.isNotEmpty()
            ) {
                Text("Save")
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(lines) { index, line ->
                Text(
                    text = line,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (index == currentLine) Color.Yellow else Color.Transparent)
                        .padding(4.dp)
                )
            }
        }
    }
}

private fun playBook(
    session: OrtSession,
    phonemeConverter: PhonemeConverter,
    lines: List<String>,
    audioPlayer: AudioPlayer,
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onLineChanged: (Int) -> Unit,
    onFinished: () -> Unit
) {
    scope.launch(Dispatchers.IO) {
        for ((index, line) in lines.withIndex()) {
            onLineChanged(index)
            val phonemes = phonemeConverter.phonemize(line)
            val (audio, _) = createAudio(
                phonemes = phonemes,
                voice = "af_sarah",
                speed = 1.0f,
                session = session,
                context = context
            )
            audioPlayer.prepare(audio)
            audioPlayer.play()
        }
        onLineChanged(-1)
        withContext(Dispatchers.Main) { onFinished() }
    }
}

private suspend fun readTextFromUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
}
