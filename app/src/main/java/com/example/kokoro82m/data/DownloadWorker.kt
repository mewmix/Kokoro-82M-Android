package com.example.kokoro82m.data

import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.kokoro82m.utils.DebugLogger
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class DownloadWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val CHANNEL_ID = "model_download"
        private const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result {
        DebugLogger.initialize(applicationContext)
        val modelId = inputData.getString("model_id")
        val downloadUrl = inputData.getString("download_url")
        val hfToken = inputData.getString("hf_token")
        val modelName = inputData.getString("model_name") ?: modelId

        if (modelId.isNullOrEmpty() || downloadUrl.isNullOrEmpty()) {
            return Result.failure()
        }

        createChannel()
        setForeground(createForegroundInfo(0, modelName!!))

        return try {
            val modelDir = File(applicationContext.filesDir, "models")
            if (!modelDir.exists()) {
                modelDir.mkdirs()
            }
            val finalFile = File(modelDir, "$modelId.task")
            val tempFile = File(modelDir, "$modelId.task.part")

            DebugLogger.log("DownloadWorker: Starting download for model '$modelId' from: $downloadUrl")

            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpsURLConnection
            hfToken?.let {
                connection.setRequestProperty("Authorization", "Bearer $it")
                DebugLogger.log("DownloadWorker: Authorization header set for Hugging Face.")
            }
            val existing = if (tempFile.exists()) tempFile.length() else 0L
            if (existing > 0) {
                connection.setRequestProperty("Range", "bytes=$existing-")
            }
            connection.connect()

            val totalLen = connection.getHeaderFieldInt("Content-Length", -1)
            val total = if (totalLen > 0) totalLen + existing else -1
            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(tempFile, existing > 0)

            val buffer = ByteArray(1024)
            var len: Int
            var downloaded = existing
            while (inputStream.read(buffer).also { len = it } != -1) {
                outputStream.write(buffer, 0, len)
                downloaded += len
                if (total > 0) {
                    val progress = downloaded.toFloat() / total
                    setProgress(workDataOf("progress" to progress))
                    setForeground(createForegroundInfo((progress * 100).toInt(), modelName))
                }
            }

            outputStream.close()
            inputStream.close()

            tempFile.renameTo(finalFile)

            val finished = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle("$modelName downloaded")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .build()
            NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, finished)

            DebugLogger.log("DownloadWorker: Model '$modelId' downloaded successfully to ${finalFile.absolutePath}")
            Result.success()
        } catch (e: Exception) {
            DebugLogger.log("DownloadWorker: Error downloading model '$modelId' from $downloadUrl - ${e.message}")
            Result.failure()
        }
    }

    private fun createForegroundInfo(progress: Int, modelName: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Downloading $modelName")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setProgress(100, progress, progress == 0)
            .build()
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Model Downloads",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
