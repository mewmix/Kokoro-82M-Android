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

package com.google.ai.edge.gallery.ui.common.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BenchmarkConfigDialog(
  onDismiss: () -> Unit,
  onConfirm: (Int) -> Unit,
) {
  var iterations by remember { mutableStateOf(1) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Benchmark Configuration") },
    text = {
      Column {
        Text("Iterations: $iterations")
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
          value = iterations.toFloat(),
          onValueChange = { iterations = it.toInt() },
          valueRange = 1f..10f,
          steps = 9,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          onConfirm(iterations)
          onDismiss()
        }
      ) {
        Text("Run")
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}
