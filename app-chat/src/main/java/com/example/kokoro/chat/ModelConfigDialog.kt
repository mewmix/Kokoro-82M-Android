package com.example.kokoro.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ModelConfigDialog(
    config: ModelConfig,
    onDismiss: () -> Unit,
    onApply: (ModelConfig) -> Unit
) {
    var temperature by remember { mutableStateOf(config.temperature) }
    var topK by remember { mutableStateOf(config.topK.toFloat()) }
    var topP by remember { mutableStateOf(config.topP) }
    var maxTokens by remember { mutableStateOf(config.maxTokens.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onApply(
                    ModelConfig(
                        temperature = temperature,
                        topK = topK.toInt(),
                        topP = topP,
                        maxTokens = maxTokens.toInt()
                    )
                )
            }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Model Settings") },
        text = {
            Column {
                Text("Temperature: ${"%.2f".format(temperature)}")
                Slider(value = temperature, onValueChange = { temperature = it }, valueRange = 0f..1f)
                Spacer(Modifier.height(8.dp))
                Text("Top K: ${topK.toInt()}")
                Slider(value = topK, onValueChange = { topK = it }, valueRange = 1f..100f, steps = 99)
                Spacer(Modifier.height(8.dp))
                Text("Top P: ${"%.2f".format(topP)}")
                Slider(value = topP, onValueChange = { topP = it }, valueRange = 0f..1f)
                Spacer(Modifier.height(8.dp))
                Text("Max Tokens: ${maxTokens.toInt()}")
                Slider(value = maxTokens, onValueChange = { maxTokens = it }, valueRange = 16f..4096f)
            }
        }
    )
}
