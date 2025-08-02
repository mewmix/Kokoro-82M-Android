package com.example.kokoro82m.data

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.getWorkInfoByIdFlow
import androidx.work.workDataOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import com.example.kokoro82m.utils.DebugLogger

class ModelDownloader(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val _progress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val progress: StateFlow<Map<String, Float>> = _progress

    private val workManager = WorkManager.getInstance(context)

    fun downloadModel(model: Model) {
        scope.launch {
            val token = userPreferencesRepository.hfToken.first()
            val modelDir = File(context.filesDir, "models")
            if (!modelDir.exists()) modelDir.mkdirs()
            val finalFile = File(modelDir, "${model.id}.task")
            val tempFile = File(modelDir, "${model.id}.task.part")

            if (finalFile.exists()) {
                model.isDownloaded = true
                model.hasPartial = false
                model.localPath = finalFile.absolutePath
                DebugLogger.log("ModelDownloader: ${model.name} already downloaded")
                return@launch
            }

            model.hasPartial = tempFile.exists()

            val request = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(
                    workDataOf(
                        "model_id" to model.id,
                        "download_url" to model.downloadUrl,
                        "hf_token" to token,
                        "model_name" to model.name
                    )
                )
                .build()
            workManager.enqueue(request)

            workManager.getWorkInfoByIdFlow(request.id).collect { info ->
                val p = info.progress.getFloat("progress", -1f)
                if (p >= 0f) {
                    _progress.value = _progress.value.toMutableMap().apply { put(model.id, p) }
                }
                if (info.state.isFinished) {
                    _progress.value = _progress.value.toMutableMap().apply { remove(model.id) }
                    model.isDownloaded = finalFile.exists()
                    model.hasPartial = !model.isDownloaded && tempFile.exists()
                    model.localPath = if (model.isDownloaded) finalFile.absolutePath else null
                }
            }
        }
    }
}
