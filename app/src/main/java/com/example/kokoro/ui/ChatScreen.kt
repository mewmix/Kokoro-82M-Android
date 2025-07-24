package com.example.kokoro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kokoro.galleryport.PerfHud
import com.example.kokoro82m.viewmodel.MainViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ChatScreen(viewModel: MainViewModel, hud: Boolean) {
    val msgs by viewModel.chatMessages.collectAsState()
    val modelReady by viewModel.isLlmInitialized.collectAsState()

    Scaffold { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (!modelReady) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Chat model not downloaded.",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please go to the 'Models' screen to download the 'Gemma 3n IT 4B' model.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                LazyColumn(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                    items(msgs) { Text(it) }
                }
                var txt by remember { mutableStateOf("") }
                Row(modifier = Modifier.padding(8.dp)) {
                    TextField(txt, { txt = it }, Modifier.weight(1f))
                    Button(onClick = { viewModel.sendChatMessage(txt); txt = "" }) { Text("Send") }
                }
            }
        }
        if (hud) PerfHud.Overlay()
    }
}
