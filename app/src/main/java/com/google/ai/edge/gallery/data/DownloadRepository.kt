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
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.ai.edge.gallery.worker.DownloadWorker
import com.google.common.util.concurrent.ListenableFuture
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DownloadRepository(
  private val context: Context,
  private val externalScope: CoroutineScope,
  private val dispatcher: CoroutineDispatcher,
) {
  private val workManager = WorkManager.getInstance(context)

  private val _isDownloading = MutableStateFlow(false)
  val isDownloading = _isDownloading.asStateFlow()

  fun download(model: Model) {
    externalScope.launch(dispatcher) {
      val data = workDataOf("modelId" to model.modelId, "modelFile" to model.modelFile)

      val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

      val downloadWorkRequest =
        OneTimeWorkRequestBuilder<DownloadWorker>()
          .setInputData(data)
          .setConstraints(constraints)
          .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
          .build()

      workManager.enqueueUniqueWork(
        model.modelId, // Use modelId as unique work name
        ExistingWorkPolicy.KEEP, // Keep existing work if it's already running
        downloadWorkRequest,
      )
    }
  }

  fun getDownloadRequest(modelId: String): ListenableFuture<List<WorkInfo>> {
    return workManager.getWorkInfosForUniqueWork(modelId)
  }

  fun cancelDownload(modelId: String) {
    workManager.cancelUniqueWork(modelId)
  }

  fun getWorkInfoById(id: UUID): ListenableFuture<WorkInfo> {
    return workManager.getWorkInfoById(id)
  }

  companion object {
    private const val TAG = "DownloadRepository"
  }
}
