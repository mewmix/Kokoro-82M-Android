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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AudioPlaybackPanel(
  isPlaying: Boolean,
  onPlay: () -> Unit,
  onStop: () -> Unit,
  progress: Float,
  onProgressChange: (Float) -> Unit,
) {
  Row(modifier = Modifier.padding(16.dp)) {
    IconButton(onClick = if (isPlaying) onStop else onPlay) {
      Icon(
        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
        contentDescription = if (isPlaying) "Stop" else "Play",
      )
    }
    Slider(value = progress, onValueChange = onProgressChange)
  }
}
