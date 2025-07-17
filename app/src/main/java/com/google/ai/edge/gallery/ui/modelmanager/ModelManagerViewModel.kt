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

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.edge.gallery.data.Model
import com.google.ai.edge.gallery.data.parseModels
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ModelManagerViewModel @Inject constructor(@ApplicationContext private val context: Context) :
  ViewModel() {
  private val _models = MutableStateFlow<List<Model>>(emptyList())
  val models = _models.asStateFlow()

  init {
    loadModels()
  }

  private fun loadModels() {
    viewModelScope.launch {
      val jsonString =
        context.assets.open("model_allowlist.json").bufferedReader().use { it.readText() }
      _models.value = parseModels(jsonString)
    }
  }

  fun onActionClick(model: Model) {
    // TODO: Implement model download/delete logic
  }
}
