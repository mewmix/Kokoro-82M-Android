package com.example.kokoro.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kokoro.galleryport.LlmController
import com.example.kokoro.galleryport.PerfHud
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(ctx: Context, hud: Boolean) {
    val vm: ChatVM = viewModel { ChatVM(ctx) }
    val msgs by vm.msgs.collectAsState()
    val modelReady by vm.modelReady.collectAsState()

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
                        text = "Please go to the 'Models' screen to download the 'Gemma 2B IT (CPU)' model.",
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
                    Button(onClick = { vm.send(txt); txt = "" }) { Text("Send") }
                }
            }
        }
        if (hud) PerfHud.Overlay()
    }
}

class ChatVM(private val ctx: Context) : ViewModel() {
    private val _msgs = MutableStateFlow(listOf<String>())
    val msgs: StateFlow<List<String>> = _msgs

    private val _modelReady = MutableStateFlow(false)
    val modelReady: StateFlow<Boolean> = _modelReady

    private var llmController: LlmController? = null

    init {
        viewModelScope.launch {
            llmController = LlmController.bootstrap(ctx)
            _modelReady.value = llmController != null
            if (llmController == null) {
                _msgs.value = listOf("Model not found. Please download it from the Models screen.")
            } else {
                _msgs.value = listOf("Model ready.")
            }
        }
    }

    fun send(prompt: String) {
        if (llmController == null) return

        viewModelScope.launch {
            _msgs.value += "> $prompt"
            var currentResponse = ""
            llmController!!.stream(prompt).collect { tok ->
                currentResponse += tok
                _msgs.value = _msgs.value.dropLast(1) + ("> $prompt\n$currentResponse")
            }
        }
    }
}
