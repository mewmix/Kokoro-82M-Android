/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ai.edge.gallery.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.ai.edge.gallery.data.Config

@Composable
fun ConfigDialog(
  config: Config,
  onDismiss: () -> Unit,
  onConfirm: (Config) -> Unit,
) {
  var topK by remember { mutableStateOf(config.topK) }
  var topP by remember { mutableStateOf(config.topP) }
  var temperature by remember { mutableStateOf(config.temperature) }
  var maxTokens by remember { mutableStateOf(config.maxTokens) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Configuration") },
    text = {
      Column {
        ConfigSlider(
          label = "Top K",
          value = topK.toFloat(),
          onValueChange = { topK = it.toInt() },
          range = 1f..100f,
          steps = 99,
        )
        ConfigSlider(
          label = "Top P",
          value = topP,
          onValueChange = { topP = it },
          range = 0f..1f,
          steps = 100,
        )
        ConfigSlider(
          label = "Temperature",
          value = temperature,
          onValueChange = { temperature = it },
          range = 0f..1f,
          steps = 100,
        )
        ConfigSlider(
          label = "Max Tokens",
          value = maxTokens.toFloat(),
          onValueChange = { maxTokens = it.toInt() },
          range = 1f..4096f,
          steps = 4095,
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          onConfirm(Config(topK, topP, temperature, maxTokens))
          onDismiss()
        }
      ) {
        Text("Confirm")
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Dismiss") } },
  )
}

@Composable
fun ConfigSlider(
  label: String,
  value: Float,
  onValueChange: (Float) -> Unit,
  range: ClosedFloatingPointRange<Float>,
  steps: Int,
) {
  Column(modifier = Modifier.padding(vertical = 8.dp)) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text(label)
      Text(String.format("%.2f", value))
    }
    Spacer(modifier = Modifier.height(4.dp))
    Slider(
      value = value,
      onValueChange = onValueChange,
      valueRange = range,
      steps = steps,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}
