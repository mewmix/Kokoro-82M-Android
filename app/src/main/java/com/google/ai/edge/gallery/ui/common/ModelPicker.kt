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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.ai.edge.gallery.data.Model

@Composable
fun ModelPicker(models: List<Model>, selectedModel: Model, onModelSelected: (Model) -> Unit) {
  var expanded by remember { mutableStateOf(false) }

  Column {
    Row(modifier = Modifier.padding(16.dp)) {
      Text("Selected Model: ${selectedModel.name}")
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      models.forEach { model ->
        DropdownMenuItem(text = { Text(model.name) }, onClick = { onModelSelected(model) })
      }
    }
  }
}
