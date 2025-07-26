package com.example.kokoro82m.edgechat

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.sink
import java.io.File

class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val modelId = inputData.getString("model_id") ?: return Result.failure()
        val downloadUrl = inputData.getString("download_url") ?: return Result.failure()
        val token = inputData.getString("hf_token")
        val client = OkHttpClient()
        val reqBuilder = Request.Builder().url(downloadUrl)
        token?.let { reqBuilder.addHeader("Authorization", "Bearer $it") }
        val response = client.newCall(reqBuilder.build()).execute()
        if (!response.isSuccessful) return Result.failure()
        val modelDir = File(applicationContext.filesDir, "models").apply { mkdirs() }
        val outFile = File(modelDir, "$modelId.task")
        response.body?.source()?.use { source ->
            outFile.sink().buffer().use { sink ->
                sink.writeAll(source)
            }
        }
        return Result.success()
    }
}

