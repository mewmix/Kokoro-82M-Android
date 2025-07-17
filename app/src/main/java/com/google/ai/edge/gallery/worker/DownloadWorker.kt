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

package com.google.ai.edge.gallery.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.ai.edge.gallery.data.HUGGING_FACE_URL
import com.google.ai.edge.gallery.data.getHuggingFaceModelPageUrl
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class DownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    val modelId = inputData.getString("modelId") ?: return Result.failure()
    val modelFile = inputData.getString("modelFile") ?: return Result.failure()

    return try {
      val modelUrl = getHuggingFaceModelPageUrl(modelId)
      val url = URL("$modelUrl/resolve/main/$modelFile")
      val connection = url.openConnection()
      connection.connect()

      val inputStream = connection.getInputStream()
      val file = File(applicationContext.filesDir, modelFile)
      val outputStream = FileOutputStream(file)

      inputStream.use { input ->
        outputStream.use { output ->
          input.copyTo(output)
        }
      }

      Result.success()
    } catch (e: Exception) {
      Result.failure()
    }
  }
}
