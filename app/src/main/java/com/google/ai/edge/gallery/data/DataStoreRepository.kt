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

package com.google.ai.edge.gallery.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.google.ai.edge.gallery.SettingsSerializer
import com.google.ai.edge.gallery.proto.Settings
import com.google.ai.edge.gallery.ui.theme.Theme
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.settingsDataStore: DataStore<Settings> by
  dataStore(fileName = "settings.pb", serializer = SettingsSerializer)

class DataStoreRepository @Inject constructor(private val context: Context) {
  fun readTheme(): Theme {
    return runBlocking {
      val settings = context.settingsDataStore.data.first()
      when (settings.theme) {
        "dark" -> Theme.DARK
        "light" -> Theme.LIGHT
        else -> Theme.SYSTEM
      }
    }
  }

  suspend fun saveTheme(theme: Theme) {
    context.settingsDataStore.updateData {
      it.toBuilder().setTheme(theme.name.lowercase()).build()
    }
  }
}
