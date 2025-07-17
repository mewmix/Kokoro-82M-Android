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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AudioRecorderPanel(isRecording: Boolean, onStartRecording: () -> Unit, onStopRecording: () -> Unit) {
  Row(modifier = Modifier.padding(16.dp)) {
    IconButton(onClick = if (isRecording) onStopRecording else onStartRecording) {
      Icon(
        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
        contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
      )
    }
  }
}
