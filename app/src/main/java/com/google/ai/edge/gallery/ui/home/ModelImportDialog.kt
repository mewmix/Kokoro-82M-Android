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

package com.google.ai.edge.gallery.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ModelImportDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
  var modelId by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Import Model") },
    text = {
      Column {
        Text("Enter Hugging Face Model ID")
        TextField(value = modelId, onValueChange = { modelId = it })
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm(modelId)
          onDismiss()
        }
      ) {
        Text("Import")
      }
    },
    dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } },
  )
}
