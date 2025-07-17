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
import android.net.Uri
import com.google.ai.edge.gallery.common.getJsonResponse
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Model(
  val name: String,
  val modelId: String,
  val modelFile: String,
  val description: String,
  val sizeInBytes: Long,
  val estimatedPeakMemoryInBytes: Long,
  val version: String,
  val llmSupportImage: Boolean = false,
  val defaultConfig: Config,
  val taskTypes: List<String>,
  val isLocal: Boolean = false,
) {
  fun getModelUrl(pageUrl: String): String {
    return "$pageUrl/resolve/main/$modelFile"
  }

  fun isDownloaded(context: Context): Boolean {
    val file = File(context.filesDir, modelFile)
    return file.exists()
  }

  fun delete(context: Context): Boolean {
    val file = File(context.filesDir, modelFile)
    return file.delete()
  }

  fun getLocalModelUri(context: Context): Uri {
    val file = File(context.filesDir, modelFile)
    return Uri.fromFile(file)
  }
}

fun getHuggingFaceModelPageUrl(modelId: String): String {
  return "$HUGGING_FACE_URL/$modelId"
}

fun getHuggingFaceModel(modelId: String): Model? {
  val url = "$HUGGING_FACE_API_URL/$modelId"
  val response = getJsonResponse<Model>(url)
  return response?.jsonObj
}

fun parseModels(jsonString: String): List<Model> {
  val json = Json { ignoreUnknownKeys = true }
  return json.decodeFromString<ModelAllowlist>(jsonString).models
}
