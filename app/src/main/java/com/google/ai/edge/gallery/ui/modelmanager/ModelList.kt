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

package com.google.ai.edge.gallery.ui.modelmanager

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.google.ai.edge.gallery.data.Model
import com.google.ai.edge.gallery.ui.common.modelitem.ModelItem

@Composable
fun ModelList(models: List<Model>, onActionClick: (Model) -> Unit) {
  LazyColumn {
    items(models) { model ->
      ModelItem(model = model, onActionClick = { onActionClick(model) })
    }
  }
}
