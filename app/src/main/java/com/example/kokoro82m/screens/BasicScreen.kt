package com.example.kokoro82m.screens

import ai.onnxruntime.OrtSession
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kokoro82m.utils.DebugLogger
import com.example.kokoro82m.utils.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicScreen(
    session: OrtSession,
    onGenerateAudio: (String, String, Float, Boolean, () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("This is her warm heart, her warmest kokoro, unwavering love and comfort.") }
    var style by remember { mutableStateOf(SettingsManager.getStyle(context)) }
    var speed by remember { mutableFloatStateOf(SettingsManager.getSpeed(context)) }
    var isProcessing by remember { mutableStateOf(false) }
    var shouldSaveFile by remember { mutableStateOf(false) }

    val names = listOf(
        "af",
        "af_bella",
        "af_nicole",
        "af_sarah",
        "af_sky",
        "am_adam",
        "am_michael",
        "bf_emma",
        "bf_isabella",
        "bm_george",
        "bm_lewis"
    )
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = text,
            minLines = 3,
            maxLines = 12,
            onValueChange = { text = it },
            label = { Text("Text to speak") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text
            )
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = style,
                onValueChange = {
                    style = it
                    SettingsManager.setStyle(context, it)
                },
                label = { Text("Style") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                names.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            style = name
                            SettingsManager.setStyle(context, name)
                            expanded = false
                        }
                    )
                }
            }
        }

        Text("Speed: $speed")
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    shouldSaveFile = false
                    isProcessing = true
                    onGenerateAudio(text, style, speed, shouldSaveFile) {
                        isProcessing = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                enabled = !isProcessing
            ) {
                Text(if (isProcessing) "GPU Processing..." else "Play")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    shouldSaveFile = true
                    isProcessing = true
                    onGenerateAudio(text, style, speed, shouldSaveFile) {
                        isProcessing = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                enabled = !isProcessing
            ) {
                Text(if (isProcessing) "GPU Processing..." else "Play & Save")
            }
        }

        if (SettingsManager.isDebug(context)) {
            val logs = DebugLogger.getLogs().joinToString("\n")
            Text(logs, modifier = Modifier.fillMaxWidth())
        }
    }
}