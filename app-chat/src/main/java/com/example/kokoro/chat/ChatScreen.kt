package com.example.kokoro.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onBackPressed: () -> Unit
) {
    val chatState by viewModel.chatState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val params by viewModel.params.collectAsState()
    val speakEnabled by viewModel.isSpeakEnabled.collectAsState()
    var message by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gemma 3n IT 4B Chat") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                reverseLayout = true
            ) {
                items(chatState.reversed()) { chatMessage ->
                    MessageBubble(chatMessage)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Message") },
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (message.isNotBlank()) {
                            viewModel.sendMessage(message)
                            message = ""
                        }
                    },
                    enabled = message.isNotBlank() && !isLoading
                ) {
                    Text("Send")
                }
            }
            if (showSettings) {
                ParametersDialog(
                    initial = params,
                    speakEnabled = speakEnabled,
                    onDismiss = { showSettings = false },
                    onApply = { p, speak ->
                        viewModel.updateParams(p)
                        viewModel.setSpeakEnabled(speak)
                        showSettings = false
                    }
                )
            }
        }
    }
}

@Composable
fun MessageBubble(chatMessage: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (chatMessage.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.padding(4.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (chatMessage.isFromUser) 16.dp else 0.dp,
                bottomEnd = if (chatMessage.isFromUser) 0.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (chatMessage.isFromUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = chatMessage.message,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametersDialog(
    initial: LlmParameters,
    speakEnabled: Boolean,
    onDismiss: () -> Unit,
    onApply: (LlmParameters, Boolean) -> Unit
) {
    var topK by remember { mutableStateOf(initial.topK.toString()) }
    var temperature by remember { mutableStateOf(initial.temperature.toString()) }
    var seed by remember { mutableStateOf(initial.randomSeed.toString()) }
    var speak by remember { mutableStateOf(speakEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val params = LlmParameters(
                    topK = topK.toIntOrNull() ?: initial.topK,
                    temperature = temperature.toFloatOrNull() ?: initial.temperature,
                    randomSeed = seed.toIntOrNull() ?: initial.randomSeed
                )
                onApply(params, speak)
            }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("LLM Settings") },
        text = {
            Column {
                OutlinedTextField(
                    value = topK,
                    onValueChange = { topK = it },
                    label = { Text("Top K") }
                )
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("Temperature") }
                )
                OutlinedTextField(
                    value = seed,
                    onValueChange = { seed = it },
                    label = { Text("Random Seed") }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Speak")
                    Switch(checked = speak, onCheckedChange = { speak = it })
                }
            }
        }
    )
}
